package algorithm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import model.FastLivelinessModel;
import model.LiveInterval;

public class LinearScanRegisterAllocator {
  LinkedList<String> availableRegisters;
  int maxRegisters;
  List<LiveInterval> activeIntervals = new ArrayList<>();
  Map<String, Map<String, String>> registerAllocationTable = new HashMap<>();
  Map<String, Set<String>> spillTable = new HashMap<>();
  FastLivelinessModel fastLivelinessModel;

  public LinearScanRegisterAllocator(FastLivelinessModel fastLivelinessModel, int maxRegisters) {
    this.maxRegisters = maxRegisters;
    this.fastLivelinessModel = fastLivelinessModel;
  }

  public Map<String, Map<String, String>> getRegisterAllocationTable() {
    return registerAllocationTable;
  }

  public Map<String, Set<String>> getSpillTable() {
    return spillTable;
  }

  public void computeRegisterAllocationTable() {
    for (String funcName : fastLivelinessModel.getDefMap().keySet()) {
      computeRegisterAllocationTableForFunction(funcName);
    }
  }

  private void computeRegisterAllocationTableForFunction(String funcName) {
    initializeRegisterPool();
    registerAllocationTable.put(funcName, new HashMap<>());
    spillTable.put(funcName, new HashSet<>());
    Map<String, Integer> defMap = fastLivelinessModel.getDefMapForFunc(funcName);
    Map<String, Integer> useMap = fastLivelinessModel.getUseMapForFunc(funcName);

    List<LiveInterval> intervals = defMap.entrySet().stream()
        .map(entry -> new LiveInterval(entry.getKey(), entry.getValue(), useMap.get(entry.getKey())))
        .sorted(Comparator.comparingInt(i -> i.start))
        .collect(Collectors.toList());

    // LinearScanRegsiterAllocation algorithm:
    // https://web.cs.ucla.edu/~palsberg/course/cs132/linearscan.pdf
    activeIntervals.clear();

    for (LiveInterval interval : intervals) {
      expireOldIntervals(interval, funcName);
      if (activeIntervals.size() == maxRegisters) {
        spillAtInterval(interval, funcName);
      } else {
        String freeRegister = availableRegisters.poll();
        registerAllocationTable.get(funcName).put(interval.var, freeRegister);
        activeIntervals.add(interval);
        Collections.sort(activeIntervals, Comparator.comparingInt(i -> i.end));
      }
    }
  }

  /*
   * LinearScanRegsiterAllocation algorithm:
   * https://web.cs.ucla.edu/~palsberg/course/cs132/linearscan.pdf
   */
  private void expireOldIntervals(LiveInterval interval, String funcName) {
    Iterator<LiveInterval> it = activeIntervals.iterator();
    while (it.hasNext()) {
      LiveInterval activeInterval = it.next();
      if (activeInterval.end >= interval.start)
        break;
      String reg = registerAllocationTable.get(funcName).get(activeInterval.var);
      availableRegisters.addFirst(reg);
      it.remove();
    }
  }

  /*
   * LinearScanRegsiterAllocation algorithm:
   * https://web.cs.ucla.edu/~palsberg/course/cs132/linearscan.pdf
   */
  private void spillAtInterval(LiveInterval interval, String funcName) {
    String intervalVar = interval.var;
    LiveInterval lastActiveInterval = activeIntervals.get(activeIntervals.size() - 1);
    if (lastActiveInterval.end > interval.end) {
      String spilledVar = lastActiveInterval.var;
      String spilledVarRegister = registerAllocationTable.get(funcName).get(spilledVar);
      registerAllocationTable.get(funcName).put(intervalVar, spilledVarRegister);
      registerAllocationTable.get(funcName).remove(spilledVar);
      spillTable.get(funcName).add(spilledVar);
      activeIntervals.remove(lastActiveInterval);
      activeIntervals.add(interval);
      Collections.sort(activeIntervals, Comparator.comparingInt(i -> i.end));
    } else {
      spillTable.get(funcName).add(intervalVar);
    }
  }

  private void initializeRegisterPool() {
    availableRegisters = new LinkedList<>();

    // a2 - a7
    for (int i = 2; i <= 7 && availableRegisters.size() < maxRegisters; i++) {
      availableRegisters.add("a" + i);
    }

    // s1 - s11
    for (int i = 1; i <= 11 && availableRegisters.size() < maxRegisters; i++) {
      availableRegisters.add("s" + i);
    }

    // t2 - t5, t0 & t1 are reserved as temporary registers
    for (int i = 2; i <= 5 && availableRegisters.size() < maxRegisters; i++) {
      availableRegisters.add("t" + i);
    }
  }

  public boolean isSpilled(String funcName, String var) {
    return spillTable.containsKey(funcName) && spillTable.get(funcName).contains(var);
  }

  public String getRegister(String funcName, String var) {
    return registerAllocationTable.get(funcName).get(var);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    for (String func : registerAllocationTable.keySet()) {
      sb.append("Function: ").append(func).append("\n");

      Map<String, String> regMap = registerAllocationTable.get(func);
      if (!regMap.isEmpty()) {
        sb.append("  Register Assignments:\n");
        regMap.forEach((var, reg) -> sb.append("    ").append(var).append(" → ").append(reg).append("\n"));
      }

      Set<String> spilled = spillTable.get(func);
      if (!spilled.isEmpty()) {
        sb.append("  Spilled Variables:\n");
        spilled.forEach(var -> sb.append("    ").append(var).append("\n"));
      }
    }
    return sb.toString();
  }

}
