package context;

import java.util.HashMap;
import java.util.Map;

import IR.token.Identifier;
import symbolTable.ClassInfo;
import symbolTable.MethodInfo;
import symbolTable.SymbolTable;

public class TranslationContext {
  private final SymbolTable symbolTable;
  private final ClassInfo currentClassInfo;
  private final MethodInfo currentMethodInfo;
  private final Map<String, Identifier> localVarMap = new HashMap<>();
  private int variableCounter = 0;

  public TranslationContext(SymbolTable symbolTable, ClassInfo classInfo, MethodInfo methodInfo) {
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

  public Identifier getNextVariable() {
    String varName = "v" + variableCounter++;
    return new Identifier(varName);
  }

  /**
   * Gets the next variable by default
   */
  public Identifier addLocalVar(String name) {
    Identifier id = getNextVariable();
    localVarMap.put(name, id);
    return id;
  }

  public Identifier lookupVar(String name) {
    Identifier id = localVarMap.get(name);
    if (id == null) {
      throw new IllegalStateException("Variable '" + name + "' not found in current method context.");
    }
    return id;
  }

  public boolean hasVar(String name) {
    return localVarMap.containsKey(name);
  }
}
