package symbolTable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
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

  public List<String> getParameterNames() {
    return new ArrayList<>(parameters.keySet());
  }

  public List<MJType> getParameterTypes() {
    return new ArrayList<>(parameters.values());
  }

  public Boolean hasParameter(String paramName) {
    return parameters.containsKey(paramName);
  }

  public MJType getParameterType(String paramName) {
    if (parameters.containsKey(paramName)) {
      return parameters.get(paramName);
    }

    return null;
  }

  public void addParameter(String paramName, MJType paramType) {
    if (parameters.containsKey(paramName)) {
      OutputMessage.outputErrorAndExit();
    }
    parameters.put(paramName, paramType);
  }

  public Map<String, MJType> getLocalVariables() {
    return localVariables;
  }

  public Boolean hasLocalVariable(String varName) {
    return localVariables.containsKey(varName);
  }

  public MJType getLocalVariableType(String varName) {
    if (localVariables.containsKey(varName)) {
      return localVariables.get(varName);
    }

    return null;
  }

  public void addLocalVariable(String varName, MJType varType) {
    if (parameters.containsKey(varName)) {
      OutputMessage.outputErrorAndExit();
    }

    if (localVariables.containsKey(varName)) {
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
