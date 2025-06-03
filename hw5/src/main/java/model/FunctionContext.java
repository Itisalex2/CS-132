package model;

import java.util.LinkedHashSet;

public class FunctionContext {
  private int parameterCount;
  private LinkedHashSet<String> localVariables;

  public FunctionContext(int parameterCount, LinkedHashSet<String> localVariables) {
    this.parameterCount = parameterCount;
    this.localVariables = localVariables;
  }

  public int getParameterCount() {
    return parameterCount;
  }

  public LinkedHashSet<String> getLocalVariables() {
    return localVariables;
  }

  public void addLocalVariable(String variableName) {
    localVariables.add(variableName);
  }

  public int getFrameSize() {
    return 8 + localVariables.size() * 4;
  }

  @Override
  public String toString() {
    return "FunctionContext{" +
        "parameterCount=" + parameterCount +
        ", localVariables=" + localVariables +
        "frameSize=" + getFrameSize() +
        "}";
  }
}
