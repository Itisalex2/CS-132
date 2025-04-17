package myVisitor;

import context.TypecheckContext;
import minijava.syntaxtree.ClassDeclaration;
import minijava.syntaxtree.Goal;
import minijava.syntaxtree.MainClass;
import minijava.syntaxtree.NodeListOptional;
import minijava.visitor.GJDepthFirst;
import symbolTable.ClassInfo;
import symbolTable.MethodInfo;
import symbolTable.SymbolTable;
import type.MJType;

public class TypecheckVisitor extends GJDepthFirst<MJType, TypecheckContext> {
  @Override
  public MJType visit(Goal n, TypecheckContext typecheckContext) {
    MainClass mainclass = n.f0;
    NodeListOptional typeDeclarations = n.f1;

    mainclass.accept(this, typecheckContext);
    typeDeclarations.accept(this, typecheckContext);

    return null;
  }

  @Override
  public MJType visit(MainClass n, TypecheckContext typecheckContext) {
    SymbolTable symbolTable = typecheckContext.getSymbolTable();
    String className = n.f1.f0.toString();
    NodeListOptional statements = n.f15;

    ClassInfo classInfo = symbolTable.getClassInfo(className);
    MethodInfo mainMethodInfo = classInfo.getMainMethodInfo();

    TypecheckContext newTypeCheckContext = new TypecheckContext(symbolTable, classInfo, mainMethodInfo);
    statements.accept(this, newTypeCheckContext);

    return null;
  }

  @Override
  public MJType visit(ClassDeclaration n, TypecheckContext typecheckContext) {
    SymbolTable symbolTable = typecheckContext.getSymbolTable();
    String className = n.f1.f0.toString();
    NodeListOptional methodDeclarations = n.f4;

    ClassInfo classInfo = symbolTable.getClassInfo(className);

    TypecheckContext newTypeCheckContext = new TypecheckContext(symbolTable, classInfo, null);
    methodDeclarations.accept(this, newTypeCheckContext);

    return null;
  }
}
