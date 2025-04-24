package symbolTable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import constant.OutputMessage;

public class SymbolTable {
  private Map<String, ClassInfo> symbolTable = new HashMap<>();

  public void addClass(String className, ClassInfo classInfo) {
    if (symbolTable.containsKey(className)) {
      System.err.println("Class " + className + " already exists in the symbol table.");
      OutputMessage.outputErrorAndExit();
    }

    symbolTable.put(className, classInfo);
  }

  public ClassInfo getClassInfo(String className) {
    if (symbolTable.containsKey(className)) {
      return symbolTable.get(className);
    }

    System.err.println("Class " + className + " not found in the symbol table.");
    OutputMessage.outputErrorAndExit();
    throw new AssertionError("Unreachable");
  }

  public boolean containsCycle() {
    for (ClassInfo current : symbolTable.values()) {
      Set<String> visited = new HashSet<>();
      String className = current.getClassName();
      visited.add(className);

      String superClassName = current.getSuperClassName();
      while (superClassName != null) {
        if (visited.contains(superClassName)) {
          return true;
        }
        visited.add(superClassName);
        ClassInfo parent = getClassInfo(superClassName);
        superClassName = parent.getSuperClassName();
      }
    }
    return false;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("Symbol Table:\n");
    for (Map.Entry<String, ClassInfo> entry : symbolTable.entrySet()) {
      sb.append("Class: ").append(entry.getKey()).append("\n");
      sb.append(entry.getValue()).append("\n");
    }
    return sb.toString();
  }
}
