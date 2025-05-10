package symbolTable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import constant.OutputMessage;
import type.ClassType;
import type.MJType;

public class SymbolTable {
  private Map<String, ClassInfo> symbolTable = new HashMap<>();

  public void addClass(String className, ClassInfo classInfo) {
    if (symbolTable.containsKey(className)) {
      OutputMessage.outputErrorAndExit();
    }

    symbolTable.put(className, classInfo);
  }

  public ClassInfo getClassInfo(String className) {
    if (symbolTable.containsKey(className)) {
      return symbolTable.get(className);
    }

    OutputMessage.outputErrorAndExit();
    throw new AssertionError("Unreachable");
  }

  public boolean containsCycle() {
    for (ClassInfo current : symbolTable.values()) {
      Set<String> visited = new HashSet<>();
      String className = current.getClassName();
      visited.add(className);

      String superClassName = current.getSuperClassName();
      while (superClassName != null) {
        if (visited.contains(superClassName)) {
          return true;
        }
        visited.add(superClassName);
        ClassInfo parent = getClassInfo(superClassName);
        superClassName = parent.getSuperClassName();
      }
    }
    return false;
  }

  public boolean containsOverload() {
    // Overloading per class is taken care of by making sure the new method
    // names don't overlap with the exisiting. We need to check for
    // overloading when extending another class.

    for (ClassInfo currentClass : symbolTable.values()) {
      for (MethodInfo currentMethod : currentClass.getMethods().values()) {
        String methodName = currentMethod.getMethodName();
        String superClassName = currentClass.getSuperClassName();

        while (superClassName != null) {
          ClassInfo parent = getClassInfo(superClassName);

          if (parent.containsMethod(methodName)) {
            MethodInfo parentMethod = parent.getMethodInfo(methodName);

            List<MJType> currentParams = currentMethod.getParameterTypes();
            List<MJType> parentParams = parentMethod.getParameterTypes();
            if (!currentParams.equals(parentParams)) {
              return true;
            }

            MJType currentReturnType = currentMethod.getReturnType();
            MJType parentReturnType = parentMethod.getReturnType();

            if (!currentReturnType.equals(parentReturnType)) {
              return true;
            }

            break;
          }

          superClassName = parent.getSuperClassName();
        }
      }
    }
    return false;
  }

  public boolean isSubtype(MJType subType, MJType superType) {
    if (subType.equals(superType)) {
      return true;
    }

    if (!(subType instanceof ClassType) || !(superType instanceof ClassType)) {
      return false;
    }

    String subName = ((ClassType) subType).getName();
    String superName = ((ClassType) superType).getName();

    while (subName != null) {
      if (subName.equals(superName)) {
        return true;
      }

      ClassInfo info = getClassInfo(subName);
      if (info == null)
        return false;
      subName = info.getSuperClassName();
    }
    return false;
  }

  public boolean allTypesExist() {
    for (ClassInfo classInfo : symbolTable.values()) {
      for (MJType fieldType : classInfo.getFields().values()) {
        if (fieldType instanceof ClassType) {
          String name = ((ClassType) fieldType).getName();
          if (!symbolTable.containsKey(name)) {
            return false;
          }
        }
      }
      for (MethodInfo method : classInfo.getMethods().values()) {
        MJType returnType = method.getReturnType();
        if (returnType instanceof ClassType) {
          String name = ((ClassType) returnType).getName();
          if (!symbolTable.containsKey(name)) {
            return false;
          }
        }

        for (MJType paramType : method.getParameterTypes()) {
          if (paramType instanceof ClassType) {
            String name = ((ClassType) paramType).getName();
            if (!symbolTable.containsKey(name)) {
              return false;
            }
          }
        }

        for (MJType localType : method.getLocalVariables().values()) {
          if (localType instanceof ClassType) {
            String name = ((ClassType) localType).getName();
            if (!symbolTable.containsKey(name)) {
              return false;
            }
          }
        }
      }
    }

    return true;
  }

  public void resolveInheritance() {
    List<ClassInfo> topoOrder = new ArrayList<>();
    Set<String> visited = new HashSet<>();

    for (String className : symbolTable.keySet()) {
      dfsTopo(className, visited, topoOrder);
    }

    for (ClassInfo cls : topoOrder) {
      String parentName = cls.getSuperClassName();

      if (parentName == null) {
        int fieldOffset = 4;
        for (String f : cls.getDeclaredFields()) {
          cls.getFieldOffsets().put(f, fieldOffset);
          fieldOffset += 4;
        }

        int methodOffset = 0;
        for (String m : cls.getDeclaredMethods()) {
          cls.getVtableOffsets().put(m, methodOffset);
          methodOffset += 4;
        }
        continue;
      }

      ClassInfo parent = getClassInfo(parentName);

      cls.getFieldOffsets().putAll(parent.getFieldOffsets());
      cls.getFields().putAll(parent.getFields());
      cls.getVtableOffsets().putAll(parent.getVtableOffsets());

      int nextFieldOffset = cls.getFieldOffsets()
          .values()
          .stream()
          .max(Integer::compare)
          .orElse(0) + 4;

      for (String f : cls.getDeclaredFields()) {
        /*
         * Always allocate a new slot – even if the name shadows
         * a parent field.
         */
        cls.getFieldOffsets().put(f, nextFieldOffset);
        nextFieldOffset += 4;
      }

      int nextMethodOffset = cls.getVtableOffsets()
          .values()
          .stream()
          .max(Integer::compare)
          .orElse(-4) + 4;

      for (String m : cls.getDeclaredMethods()) {
        if (cls.getVtableOffsets().containsKey(m)) {
          continue;
        }
        cls.getVtableOffsets().put(m, nextMethodOffset);
        nextMethodOffset += 4;
      }
    }
  }

  private void dfsTopo(String className, Set<String> visited, List<ClassInfo> topoOrder) {
    if (visited.contains(className))
      return;
    visited.add(className);

    ClassInfo classInfo = getClassInfo(className);
    String parent = classInfo.getSuperClassName();
    if (parent != null) {
      dfsTopo(parent, visited, topoOrder);
    }

    topoOrder.add(classInfo);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("Symbol Table:\n");
    for (Map.Entry<String, ClassInfo> entry : symbolTable.entrySet()) {
      sb.append("Class: ").append(entry.getKey()).append("\n");
      sb.append(entry.getValue()).append("\n");
    }
    return sb.toString();
  }
}
