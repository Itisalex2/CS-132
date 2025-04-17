package context;

import symbolTable.ClassInfo;
import symbolTable.MethodInfo;
import symbolTable.SymbolTable;

public class TypecheckContext {
  private final SymbolTable symbolTable;
  private final ClassInfo currentClass;
  private final MethodInfo currentMethod;

  public TypecheckContext(SymbolTable symbolTable, ClassInfo classInfo, MethodInfo methodInfo) {
    this.symbolTable = symbolTable;
    this.currentClass = classInfo;
    this.currentMethod = methodInfo;
  }

  public SymbolTable getSymbolTable() {
    return symbolTable;
  }

  public ClassInfo getCurrentClass() {
    return currentClass;
  }

  public MethodInfo getCurrentMethod() {
    return currentMethod;
  }
}
