package context;

import java.util.HashMap;
import java.util.Map;

import IR.token.Identifier;
import IR.token.Label;
import symbolTable.ClassInfo;
import symbolTable.MethodInfo;
import symbolTable.SymbolTable;
import type.MJType;

public class TranslationContext {
  private final SymbolTable symbolTable;
  private final ClassInfo currentClassInfo;
  private final MethodInfo currentMethodInfo;
  private final Map<String, Identifier> localVarMap = new HashMap<>();
  private final Map<String, MJType> localTypeMap = new HashMap<>();
  private int variableCounter = 0;
  private static int labelCounter = 0;

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

  public static Label getNextUniqueLabel(String base) {
    return new Label(base + "_" + (labelCounter++));
  }

  public Identifier getNextVariable() {
    String varName = "v" + variableCounter++;
    return new Identifier(varName);
  }

  /**
   * Gets the next variable by default
   */
  public Identifier addLocalVar(String name) {
    return addLocalVar(name, null);
  }

  /**
   * Gets the next variable by default
   */
  public Identifier addLocalVar(String name, MJType type) {
    Identifier id = getNextVariable();
    localVarMap.put(name, id);
    localTypeMap.put(name, type);
    return id;
  }

  public Identifier lookupVar(String name) {
    Identifier id = localVarMap.get(name);
    if (id == null) {
      throw new IllegalStateException("Variable '" + name + "' not found in current method context.");
    }
    return id;
  }

  public MJType getVarType(String name) {
    MJType t = localTypeMap.get(name); // check translator’s own map first
    if (t != null)
      return t;

    MethodInfo method = currentMethodInfo;

    if (method != null && method.hasLocalVariable(name)) {
      return method.getLocalVariableType(name);
    }
    if (method != null && method.hasParameter(name)) {
      return method.getParameterType(name);
    }
    if (currentClassInfo.containsField(name)) {
      return currentClassInfo.getFieldType(name);
    }
    throw new RuntimeException("No type found for variable: " + name);
  }

  public boolean hasVar(String name) {
    return localVarMap.containsKey(name);
  }
}
