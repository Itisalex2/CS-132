package symbolTable;

import java.util.HashMap;
import java.util.Map;

import type.MJType;
import constant.OutputMessage;

public class ClassInfo {
  private String className;
  // private String superClassName;
  private Map<String, MJType> fields = new HashMap<String, MJType>();
  private Map<String, MethodInfo> methods = new HashMap<String, MethodInfo>();

  public ClassInfo(String className) {
    this.className = className;
  }

  public void addField(String fieldName, MJType fieldType) {
    if (fields.containsKey(fieldName)) {
      System.err.println("Field " + fieldName + " already exists in class " + className);
      OutputMessage.outputErrorAndExit();
    }

    fields.put(fieldName, fieldType);
  }

  public void addMethod(String methodName, MethodInfo methodInfo) {
    if (methods.containsKey(methodName)) {
      System.err.println("Method " + methodName + " already exists in class " + className);
      OutputMessage.outputErrorAndExit();
    }

    methods.put(methodName, methodInfo);
  }

  public String getClassName() {
    return className;
  }

  public Map<String, MJType> getFields() {
    return fields;
  }

  public Map<String, MethodInfo> getMethods() {
    return methods;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("ClassInfo{name='").append(className).append("', fields={");
    fields.forEach((fieldName, fieldType) -> sb.append(fieldName).append(": ").append(fieldType).append(", "));
    if (!fields.isEmpty()) {
      sb.setLength(sb.length() - 2);
    }
    sb.append("}, methods={");
    methods.forEach((methodName, methodInfo) -> sb.append(methodName).append(": ").append(methodInfo).append(", "));
    if (!methods.isEmpty()) {
      sb.setLength(sb.length() - 2);
    }
    sb.append("}}");
    return sb.toString();
  }
}
