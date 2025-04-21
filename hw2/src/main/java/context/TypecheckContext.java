package context;

import symbolTable.ClassInfo;
import symbolTable.MethodInfo;
import symbolTable.SymbolTable;

public class TypecheckContext {
  private final SymbolTable symbolTable;
  private final ClassInfo currentClassInfo;
  private final MethodInfo currentMethodInfo;

  public TypecheckContext(SymbolTable symbolTable, ClassInfo classInfo, MethodInfo methodInfo) {
    this.symbolTable = symbolTable;
    this.currentClassInfo = classInfo;
    this.currentMethodInfo = methodInfo;
  }

  public SymbolTable getSymbolTable() {
    return symbolTable;
  }

  public ClassInfo getCurrentClassInfo() {
    return currentClassInfo;
  }

  public MethodInfo getCurrentMethodInfo() {
    return currentMethodInfo;
  }
}
