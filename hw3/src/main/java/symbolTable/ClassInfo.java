package symbolTable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import type.MJType;
import constant.OutputMessage;

public class ClassInfo {
  private final String className;
  private String superClassName = null;
  private final Map<String, MJType> fields = new HashMap<>();
  private final Map<String, Integer> fieldOffsets = new HashMap<>();
  private final Map<String, MethodInfo> methods = new HashMap<>();
  private final List<String> vtableMethods = new ArrayList<>();
  private final Map<String, Integer> vtableOffsets = new HashMap<>();

  public ClassInfo(String className) {
    this.className = className;

    fields.put("__vmt", null); // First field is always the vtable pointer
    fieldOffsets.put("__vmt", 0);
  }

  public void addField(String fieldName, MJType fieldType) {
    if (fields.containsKey(fieldName)) {
      OutputMessage.outputErrorAndExit();
    }

    fields.put(fieldName, fieldType);
    fieldOffsets.put(fieldName, fieldOffsets.size() * 4);
  }

  public void addMethod(String methodName, MethodInfo methodInfo) {
    if (methods.containsKey(methodName)) {
      OutputMessage.outputErrorAndExit();
    }

    methods.put(methodName, methodInfo);
    vtableMethods.add(methodName);
    vtableOffsets.put(methodName, vtableOffsets.size() * 4);
  }

  public String getClassName() {
    return className;
  }

  public String getSuperClassName() {
    throw new UnsupportedOperationException("Inheritance not supported yet");
    // return superClassName;
  }

  public void setSuperClassName(String superClassName) {
    throw new UnsupportedOperationException("Inheritance not supported yet");
    // if (this.superClassName != null) {
    // OutputMessage.outputErrorAndExit();
    // }
    //
    // this.superClassName = superClassName;
  }

  public Map<String, MJType> getFields() {
    return fields;
  }

  public MJType getFieldType(String fieldName) {
    if (fields.containsKey(fieldName)) {
      return fields.get(fieldName);
    }

    return null;
  }

  public boolean containsField(String fieldName) {
    return fields.containsKey(fieldName);
  }

  public int getFieldOffset(String fieldName) {
    Integer offset = fieldOffsets.get(fieldName);
    if (offset == null) {
      System.err.println("Field '" + fieldName + "' not found in class " + className);
      OutputMessage.outputErrorAndExit();
    }
    return offset;
  }

  public Map<String, MethodInfo> getMethods() {
    return methods;
  }

  public MethodInfo getMethodInfo(String methodName) {
    if (methods.containsKey(methodName)) {
      return methods.get(methodName);
    }

    OutputMessage.outputErrorAndExit();
    throw new AssertionError("Unreachable");
  }

  public MethodInfo getMainMethodInfo() {
    if (methods.containsKey("main")) {
      return methods.get("main");
    }

    OutputMessage.outputErrorAndExit();
    throw new AssertionError("Unreachable");
  }

  public int getMethodOffset(String methodName) {
    Integer offset = vtableOffsets.get(methodName);
    if (offset == null) {
      System.err.println("Method '" + methodName + "' not found in class " + className);
      OutputMessage.outputErrorAndExit();
    }
    return offset;
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
    fields.forEach((fieldName, fieldType) -> sb.append(fieldName).append(": ").append(fieldType).append(" (offset ")
        .append(getFieldOffset(fieldName)).append("), "));
    if (!fields.isEmpty())
      sb.setLength(sb.length() - 2);
    sb.append("}, methods={");
    methods.forEach((methodName, methodInfo) -> sb.append(methodName).append(": ").append(methodInfo).append(", "));
    if (!methods.isEmpty())
      sb.setLength(sb.length() - 2);
    sb.append("}}");
    return sb.toString();
  }
}
