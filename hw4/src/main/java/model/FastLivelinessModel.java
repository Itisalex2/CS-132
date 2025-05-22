package model;

import java.util.HashMap;
import java.util.Map;

/*
 * Keeps track of def and use of variables of each func in the program.
 * Keeps track of labels and gotos
 */
public class FastLivelinessModel {
  private Map<String, Map<String, Integer>> defMap = new HashMap<>();
  private Map<String, Map<String, Integer>> useMap = new HashMap<>();

  public void putDef(String funcName, String var, int line) {
    Map<String, Integer> funcDefMap = defMap.computeIfAbsent(funcName, k -> new HashMap<>());
    if (!funcDefMap.containsKey(var)) {
      funcDefMap.put(var, line);
    }
  }

  public void putUse(String funcName, String var, int line) {
    Map<String, Integer> funcUseMap = useMap.computeIfAbsent(funcName, k -> new HashMap<>());
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
