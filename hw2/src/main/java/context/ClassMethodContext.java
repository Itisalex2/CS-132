package context;

import symbolTable.ClassInfo;
import symbolTable.MethodInfo;

/**
 * When building the symbol table, we need to keep track of:
 * 
 * - The current class: used for resolving inheritance and fields.
 * - The current method: used for collecting parameters and local
 * variables.
 * 
 * @param classInfo  The class information of the current class.
 * @param methodInfo The method information of the current method. Null if not
 *                   needed
 *
 */
public class ClassMethodContext {
  private final ClassInfo classInfo;
  private final MethodInfo methodInfo;

  public ClassMethodContext(ClassInfo classInfo, MethodInfo methodInfo) {
    this.classInfo = classInfo;
    this.methodInfo = methodInfo;
  }

  public ClassInfo getClassInfo() {
    return classInfo;
  }

  public MethodInfo getMethodInfo() {
    return methodInfo;
  }

  public boolean containsMethodInfo() {
    return methodInfo != null;
  }

  @Override
  public String toString() {
    return "Class=" + classInfo.getClassName() +
        (methodInfo != null ? ", Method=" + methodInfo.getMethodName() : "");
  }
}
