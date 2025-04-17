package symbolTable;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import constant.OutputMessage;
import type.MJType;

public class MethodInfo {
  private String methodName;
  private MJType returnType;
  private LinkedHashMap<String, MJType> parameters = new LinkedHashMap<String, MJType>();
  private Map<String, MJType> localVariables = new HashMap<String, MJType>();

  public MethodInfo(String methodName, MJType returnType) {
    this.methodName = methodName;
    this.returnType = returnType;
  }

  public String getMethodName() {
    return methodName;
  }

  public MJType getReturnType() {
    return returnType;
  }

  public LinkedHashMap<String, MJType> getParameters() {
    return parameters;
  }

  public Map<String, MJType> getLocalVariables() {
    return localVariables;
  }

  public void addParameter(String paramName, MJType paramType) {
    if (parameters.containsKey(paramName)) {
      System.err.println("Parameter " + paramName + " already exists in method " + methodName);
      OutputMessage.outputErrorAndExit();
    }
    parameters.put(paramName, paramType);
  }

  public void addLocalVariable(String varName, MJType varType) {
    if (localVariables.containsKey(varName)) {
      System.err.println("Local variable " + varName + " already exists in method " + methodName);
      OutputMessage.outputErrorAndExit();
    }
    localVariables.put(varName, varType);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("MethodInfo{name='").append(methodName)
        .append("', returnType='").append(returnType)
        .append("', parameters={");

    parameters.forEach((paramName, paramType) -> sb.append(paramName).append(": ").append(paramType).append(", "));

    if (!parameters.isEmpty()) {
      sb.setLength(sb.length() - 2);
    }

    sb.append("}, localVariables={");

    localVariables.forEach((varName, varType) -> sb.append(varName).append(": ").append(varType).append(", "));

    if (!localVariables.isEmpty()) {
      sb.setLength(sb.length() - 2);
    }

    sb.append("}}");
    return sb.toString();
  }
}
