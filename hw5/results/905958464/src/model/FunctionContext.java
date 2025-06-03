package model;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.stream.Collectors;

import IR.token.Identifier;

public class FunctionContext {
  private String functionName;
  private final List<String> parameters;
  private LinkedHashSet<String> localVariables;
  private String returnId;

  public FunctionContext(String functionName, List<Identifier> parameters, LinkedHashSet<String> localVariables) {
    this.functionName = functionName;
    this.parameters = parameters.stream()
        .map(Identifier::toString)
        .collect(Collectors.toList());
    this.localVariables = localVariables;
  }

  public String getFunctionName() {
    return functionName;
  }

  public LinkedHashSet<String> getLocalVariables() {
    return localVariables;
  }

  public void addLocalVariable(String variableName) {
    localVariables.add(variableName);
  }

  public void setReturnId(String returnId) {
    this.returnId = returnId;
  }

  public int getFrameSize() {
    return 8 + localVariables.size() * 4;
  }

  public int offsetOf(String name) {
    // Parameter
    int idx = parameters.indexOf(name);
    if (idx >= 0)
      return 4 * idx;

    // Local variable
    int localIdx = 0;
    for (String id : localVariables) {
      if (id.equals(name))
        return -4 * (localIdx + 3); // –12(fp), –16(fp), …
      localIdx++;
    }
    throw new IllegalArgumentException("unknown identifier " + name);
  }

  public String getReturnId() {
    return returnId;
  }

  @Override
  public String toString() {
    return "FunctionContext{" +
        "functionName='" + functionName + '\'' +
        ", parameters=" + parameters +
        ", localVariables=" + localVariables +
        '}';
  }
}
