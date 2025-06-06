package myVisitor;

import context.ClassMethodContext;
import minijava.syntaxtree.ClassDeclaration;
import minijava.syntaxtree.ClassExtendsDeclaration;
import minijava.syntaxtree.FormalParameter;
import minijava.syntaxtree.MainClass;
import minijava.syntaxtree.MethodDeclaration;
import minijava.syntaxtree.NodeListOptional;
import minijava.syntaxtree.NodeOptional;
import minijava.syntaxtree.VarDeclaration;
import minijava.visitor.GJVoidDepthFirst;
import symbolTable.ClassInfo;
import symbolTable.MethodInfo;
import symbolTable.SymbolTable;
import type.MJType;
import type.PrimitiveType;
import type.TypeFactory;
import type.VoidType;

public class SymbolTableBuilderVisitor extends GJVoidDepthFirst<ClassMethodContext> {
  private SymbolTable symbolTable = new SymbolTable();

  public SymbolTable getSymbolTable() {
    return symbolTable;
  }

  @Override
  public String toString() {
    return symbolTable.toString();
  }

  @Override
  public void visit(MainClass n, ClassMethodContext associatedClassMethodContext) {
    NodeListOptional varDeclarations = n.f14;
    String className = n.f1.f0.toString();
    String mainMethodName = n.f6.toString();
    MJType mainMethodReturnType = VoidType.INSTANCE;
    String mainMethodParamName = n.f11.f0.toString();
    PrimitiveType mainMethodParamType = PrimitiveType.STRING_ARRAY;

    MethodInfo methodInfo = new MethodInfo(mainMethodName, mainMethodReturnType);
    methodInfo.addParameter(mainMethodParamName, mainMethodParamType);

    ClassInfo classInfo = new ClassInfo(className);
    classInfo.addMethod(mainMethodName, methodInfo);

    symbolTable.addClass(className, classInfo);

    ClassMethodContext classMethodContext = new ClassMethodContext(classInfo, methodInfo);
    varDeclarations.accept(this, classMethodContext);
  }

  @Override
  public void visit(ClassDeclaration n, ClassMethodContext associatedClassMethodContext) {
    String className = n.f1.f0.toString();
    NodeListOptional classFields = n.f3;
    NodeListOptional methodDeclarations = n.f4;

    ClassInfo classInfo = new ClassInfo(className);

    symbolTable.addClass(className, classInfo);

    ClassMethodContext classContext = new ClassMethodContext(classInfo, null);

    classFields.accept(this, classContext);
    methodDeclarations.accept(this, classContext);
  }

  @Override
  public void visit(ClassExtendsDeclaration n, ClassMethodContext associatedClassMethodContext) {
    String className = n.f1.f0.toString();
    String superClassName = n.f3.f0.toString();
    NodeListOptional classFields = n.f5;
    NodeListOptional methodDeclarations = n.f6;

    ClassInfo classInfo = new ClassInfo(className);
    classInfo.setSuperClassName(superClassName);

    symbolTable.addClass(className, classInfo);
    ClassMethodContext classContext = new ClassMethodContext(classInfo, null);

    classFields.accept(this, classContext);
    methodDeclarations.accept(this, classContext);
  }

  @Override
  public void visit(MethodDeclaration n, ClassMethodContext associatedClassMethodContext) {
    MJType returnType = TypeFactory.getTypeFromNodeChoice(n.f1.f0);
    String methodName = n.f2.f0.toString();
    ClassInfo classInfo = associatedClassMethodContext.getClassInfo();
    NodeOptional paramList = n.f4;
    NodeListOptional varList = n.f7;

    MethodInfo methodInfo = new MethodInfo(methodName, returnType);
    classInfo.addMethod(methodName, methodInfo);

    ClassMethodContext classMethodContext = new ClassMethodContext(classInfo, methodInfo);

    paramList.accept(this, classMethodContext);
    varList.accept(this, classMethodContext);
  }

  @Override
  public void visit(VarDeclaration n, ClassMethodContext associatedClassMethodContext) {
    MJType varType = TypeFactory.getTypeFromNodeChoice(n.f0.f0);
    String varName = n.f1.f0.toString();
    ClassInfo classInfo = associatedClassMethodContext.getClassInfo();
    MethodInfo methodInfo = associatedClassMethodContext.getMethodInfo();

    boolean varIsClassField = !associatedClassMethodContext.containsMethodInfo();
    if (varIsClassField) {
      classInfo.addField(varName, varType);
    } else {
      methodInfo.addLocalVariable(varName, varType);
    }
  }

  @Override
  public void visit(FormalParameter n, ClassMethodContext associatedClassMethodContext) {
    MethodInfo methodInfo = associatedClassMethodContext.getMethodInfo();
    MJType paramType = TypeFactory.getTypeFromNodeChoice(n.f0.f0);
    String paramName = n.f1.f0.toString();

    methodInfo.addParameter(paramName, paramType);
  }
}
