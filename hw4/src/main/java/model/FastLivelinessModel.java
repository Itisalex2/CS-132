package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import IR.token.Identifier;

/*
 * Keeps track of def and use of variables of each func in the program.
 * Keeps track of labels and gotos
 */
public class FastLivelinessModel {
  private Map<String, Map<String, Integer>> defMap = new HashMap<>();
  private Map<String, Map<String, Integer>> useMap = new HashMap<>();
  private final Map<String, List<Identifier>> formalMap = new HashMap<>();
  private Map<String, Set<Integer>> callLinesMap = new HashMap<>();

  public void putDef(String funcName, String var, int line) {
    Map<String, Integer> funcDefMap = defMap.computeIfAbsent(funcName, k -> new HashMap<>());
    if (!funcDefMap.containsKey(var)) {
      funcDefMap.put(var, line);
    }
  }

  public void putUse(String funcName, String var, int line) {
    Map<String, Integer> funcDefMap = defMap.computeIfAbsent(funcName, k -> new HashMap<>());
    Map<String, Integer> funcUseMap = useMap.computeIfAbsent(funcName, k -> new HashMap<>());
    if (!funcDefMap.containsKey(var)) {
      // If the variable is not defined in this function, we don't track its use
      return;
    }
    if (!funcUseMap.containsKey(var) || !(line == -1)) { // Once it has a proper use line (≥ 0), don’t overwrite it
      funcUseMap.put(var, line);
    }
  }

  public Map<String, Integer> getDefMapForFunc(String funcName) {
    return defMap.getOrDefault(funcName, new HashMap<>());
  }

  public Map<String, Integer> getUseMapForFunc(String funcName) {
    return useMap.getOrDefault(funcName, new HashMap<>());
  }

  public Map<String, Map<String, Integer>> getDefMap() {
    return defMap;
  }

  public Map<String, Map<String, Integer>> getUseMap() {
    return useMap;
  }

  public void putFormalParameters(String func, List<Identifier> params) {
    formalMap.put(func, new ArrayList<>(params));
  }

  public List<Identifier> getFormalParameters(String func) {
    return formalMap.getOrDefault(func, new ArrayList<>());
  }

  public void putCallLine(String funcName, int line) {
    callLinesMap.computeIfAbsent(funcName, k -> new HashSet<>()).add(line);
  }

  public Set<Integer> getCallLinesForFunc(String funcName) {
    return callLinesMap.getOrDefault(funcName, Collections.emptySet());
  }

  public boolean isDeadVariable(String funcName, String var) {
    Map<String, Integer> defMap = getDefMapForFunc(funcName);
    Map<String, Integer> useMap = getUseMapForFunc(funcName);

    if (!defMap.containsKey(var)) {
      return false;
    }

    int defLine = defMap.get(var);
    int useLine = useMap.getOrDefault(var, -1);

    // A variable is dead if it is defined but never used after its definition
    return useLine == -1 || useLine < defLine;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    for (String func : defMap.keySet()) {
      sb.append("Function: ").append(func).append("\n");
      sb.append("  Defs:\n");
      defMap.get(func).forEach((k, v) -> sb.append("    ").append(k).append(": ").append(v).append("\n"));
      sb.append("  Uses:\n");
      useMap.getOrDefault(func, new HashMap<>())
          .forEach((k, v) -> sb.append("    ").append(k).append(": ").append(v).append("\n"));
    }
    return sb.toString();
  }
}
