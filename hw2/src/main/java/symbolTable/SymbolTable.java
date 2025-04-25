package symbolTable;

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
      System.err.println("Class " + className + " already exists in the symbol table.");
      OutputMessage.outputErrorAndExit();
    }

    symbolTable.put(className, classInfo);
  }

  public ClassInfo getClassInfo(String className) {
    if (symbolTable.containsKey(className)) {
      return symbolTable.get(className);
    }

    System.err.println("Class " + className + " not found in the symbol table.");
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
