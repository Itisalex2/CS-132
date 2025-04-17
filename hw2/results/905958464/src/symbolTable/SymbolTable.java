package symbolTable;

import java.util.HashMap;
import java.util.Map;

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

  public Map<String, ClassInfo> getSymbolTable() {
    return symbolTable;
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
