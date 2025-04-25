package symbolTable;

import java.util.HashMap;
import java.util.Map;

import type.MJType;
import constant.OutputMessage;

public class ClassInfo {
  private String className;
  private String superClassName = null;
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

  public String getSuperClassName() {
    return superClassName;
  }

  public void setSuperClassName(String superClassName) {
    if (this.superClassName != null) {
      System.err
          .println("Super class name already set for class " + className +
              ". Superclass: " + getSuperClassName());
      OutputMessage.outputErrorAndExit();
    }

    this.superClassName = superClassName;
  }

  public Map<String, MJType> getFields() {
    return fields;
  }

  public MJType getFieldType(String fieldName) {
    if (fields.containsKey(fieldName)) {
      return fields.get(fieldName);
    }

    System.err.println("Field " + fieldName + " not found in class " + className);
    return null;
  }

  public boolean containsField(String fieldName) {
    return fields.containsKey(fieldName);
  }

  public Map<String, MethodInfo> getMethods() {
    return methods;
  }

  public MethodInfo getMethodInfo(String methodName) {
    if (methods.containsKey(methodName)) {
      return methods.get(methodName);
    }

    System.err.println("Method " + methodName + " not found in class " + className);
    OutputMessage.outputErrorAndExit();
    throw new AssertionError("Unreachable");
  }

  public MethodInfo getMainMethodInfo() {
    if (methods.containsKey("main")) {
      return methods.get("main");
    }

    System.err.println("Main method not found in class " + className);
    OutputMessage.outputErrorAndExit();
    throw new AssertionError("Unreachable");
  }

  public boolean containsMethod(String methodName) {
    return methods.containsKey(methodName);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("ClassInfo{name='").append(className);
    if (superClassName != null) {
      sb.append("', extends='").append(superClassName);
    }
    sb.append("', fields={");
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
