package myVisitor;

import java.util.ArrayList;
import java.util.List;

import constant.OutputMessage;
import context.TypecheckContext;
import minijava.syntaxtree.AllocationExpression;
import minijava.syntaxtree.AndExpression;
import minijava.syntaxtree.ArrayAllocationExpression;
import minijava.syntaxtree.ArrayAssignmentStatement;
import minijava.syntaxtree.ArrayLength;
import minijava.syntaxtree.ArrayLookup;
import minijava.syntaxtree.AssignmentStatement;
import minijava.syntaxtree.BracketExpression;
import minijava.syntaxtree.ClassDeclaration;
import minijava.syntaxtree.ClassExtendsDeclaration;
import minijava.syntaxtree.CompareExpression;
import minijava.syntaxtree.Expression;
import minijava.syntaxtree.ExpressionList;
import minijava.syntaxtree.ExpressionRest;
import minijava.syntaxtree.FalseLiteral;
import minijava.syntaxtree.Goal;
import minijava.syntaxtree.Identifier;
import minijava.syntaxtree.IfStatement;
import minijava.syntaxtree.IntegerLiteral;
import minijava.syntaxtree.MainClass;
import minijava.syntaxtree.MessageSend;
import minijava.syntaxtree.MethodDeclaration;
import minijava.syntaxtree.MinusExpression;
import minijava.syntaxtree.Node;
import minijava.syntaxtree.NodeListOptional;
import minijava.syntaxtree.NodeOptional;
import minijava.syntaxtree.NotExpression;
import minijava.syntaxtree.PlusExpression;
import minijava.syntaxtree.PrimaryExpression;
import minijava.syntaxtree.PrintStatement;
import minijava.syntaxtree.Statement;
import minijava.syntaxtree.ThisExpression;
import minijava.syntaxtree.TimesExpression;
import minijava.syntaxtree.TrueLiteral;
import minijava.syntaxtree.TypeDeclaration;
import minijava.syntaxtree.WhileStatement;
import minijava.visitor.GJDepthFirst;
import symbolTable.ClassInfo;
import symbolTable.MethodInfo;
import symbolTable.SymbolTable;
import type.ClassType;
import type.MJType;
import type.PrimitiveType;

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
  public MJType visit(TypeDeclaration n, TypecheckContext typecheckContext) {
    return n.f0.accept(this, typecheckContext);
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

  @Override
  public MJType visit(ClassExtendsDeclaration n, TypecheckContext typecheckContext) {
    SymbolTable symbolTable = typecheckContext.getSymbolTable();
    String className = n.f1.f0.toString();
    NodeListOptional methodDeclarations = n.f6;

    ClassInfo classInfo = symbolTable.getClassInfo(className);

    TypecheckContext newTypeCheckContext = new TypecheckContext(symbolTable, classInfo, null);
    methodDeclarations.accept(this, newTypeCheckContext);

    return null;
  }

  @Override
  public MJType visit(MethodDeclaration n, TypecheckContext typecheckContext) {
    SymbolTable symbolTable = typecheckContext.getSymbolTable();
    ClassInfo classInfo = typecheckContext.getCurrentClassInfo();
    String methodName = n.f2.f0.toString();
    NodeListOptional statements = n.f8;
    Expression expression = n.f10;

    MethodInfo methodInfo = classInfo.getMethodInfo(methodName);
    MJType returnType = methodInfo.getReturnType();

    TypecheckContext newTypeCheckContext = new TypecheckContext(symbolTable, classInfo, methodInfo);
    statements.accept(this, newTypeCheckContext);
    MJType type = expression.accept(this, newTypeCheckContext);
    if (!returnType.equals(type)) { // TODO: Change this to subtype later
      System.err.println("Return type mismatch in method " + methodName + " in class " + classInfo.getClassName());
      OutputMessage.outputErrorAndExit();
    }

    return null;
  }

  @Override
  public MJType visit(Statement n, TypecheckContext typecheckContext) {
    return n.f0.accept(this, typecheckContext);
  }

  @Override
  public MJType visit(AssignmentStatement n, TypecheckContext typecheckContext) { // TODO: subtyping
    Identifier identifier = n.f0;
    Expression expression = n.f2;

    MJType identifierType = identifier.accept(this, typecheckContext);
    MJType expressionType = expression.accept(this, typecheckContext);

    if (!expressionType.equals(identifierType)) {
      System.err.println("Type mismatch in assignment statement. Identifier type: " + identifierType
          + ", expression type: " + expressionType);
      OutputMessage.outputErrorAndExit();
      throw new AssertionError("Unreachable");
    }

    return null;
  }

  /*
   * id [e1] = e2
   */
  @Override
  public MJType visit(ArrayAssignmentStatement n, TypecheckContext typecheckContext) {
    Identifier identifier = n.f0;
    Expression expression1 = n.f2;
    Expression expression2 = n.f5;

    MJType identifierType = identifier.accept(this, typecheckContext);
    MJType expression1Type = expression1.accept(this, typecheckContext);
    MJType expression2Type = expression2.accept(this, typecheckContext);

    if (!(identifierType.equals(PrimitiveType.INT_ARRAY)) || !(expression1Type.equals(PrimitiveType.INT))
        || !(expression2Type.equals(PrimitiveType.INT))) {
      System.err.println("Type mismatch in array assignment statement. Identifier type: " + identifierType
          + ", expression1 type: " + expression1Type + ", expression2 type: " + expression2Type);
      OutputMessage.outputErrorAndExit();
      throw new AssertionError("Unreachable");
    }

    return null;
  }

  @Override
  public MJType visit(IfStatement n, TypecheckContext typecheckContext) {
    Expression expression = n.f2;
    Statement statement1 = n.f4;
    Statement statement2 = n.f6;

    MJType expressionType = expression.accept(this, typecheckContext);
    statement1.accept(this, typecheckContext);
    statement2.accept(this, typecheckContext);

    if (!expressionType.equals(PrimitiveType.BOOLEAN)) {
      System.err.println("Type mismatch in if statement. Expression type: " + expressionType);
      OutputMessage.outputErrorAndExit();
      throw new AssertionError("Unreachable");
    }

    return null;
  }

  @Override
  public MJType visit(WhileStatement n, TypecheckContext typecheckContext) {
    Expression expression = n.f2;
    Statement statement = n.f4;

    MJType expressionType = expression.accept(this, typecheckContext);
    statement.accept(this, typecheckContext);

    if (!expressionType.equals(PrimitiveType.BOOLEAN)) {
      System.err.println("Type mismatch in while statement. Expression type: " + expressionType);
      OutputMessage.outputErrorAndExit();
      throw new AssertionError("Unreachable");
    }

    return null;
  }

  @Override
  public MJType visit(PrintStatement n, TypecheckContext typecheckContext) {
    Expression expression = n.f2;

    MJType expressionType = expression.accept(this, typecheckContext);

    if (!expressionType.equals(PrimitiveType.INT)) {
      System.err.println("Type mismatch in print statement. Expression type: " + expressionType);
      OutputMessage.outputErrorAndExit();
      throw new AssertionError("Unreachable");
    }

    return null;
  }

  @Override
  public MJType visit(Expression n, TypecheckContext typecheckContext) {
    return n.f0.accept(this, typecheckContext);
  }

  @Override
  public MJType visit(AndExpression n, TypecheckContext typecheckContext) {
    PrimaryExpression leftExpression = n.f0;
    PrimaryExpression rightExpression = n.f2;

    MJType leftType = leftExpression.accept(this, typecheckContext);
    MJType rightType = rightExpression.accept(this, typecheckContext);

    if (!leftType.equals(PrimitiveType.BOOLEAN) || !rightType.equals(PrimitiveType.BOOLEAN)) {
      System.err.println("Type mismatch in and expression. Left type: " + leftType + ", right type: " + rightType);
      OutputMessage.outputErrorAndExit();
      throw new AssertionError("Unreachable");
    }

    return PrimitiveType.BOOLEAN;
  }

  @Override
  public MJType visit(CompareExpression n, TypecheckContext typecheckContext) {
    PrimaryExpression leftExpression = n.f0;
    PrimaryExpression rightExpression = n.f2;

    MJType leftType = leftExpression.accept(this, typecheckContext);
    MJType rightType = rightExpression.accept(this, typecheckContext);

    if (!leftType.equals(PrimitiveType.INT) || !rightType.equals(PrimitiveType.INT)) {
      System.err.println("Type mismatch in and expression. Left type: " + leftType + ", right type: " + rightType);
      OutputMessage.outputErrorAndExit();
      throw new AssertionError("Unreachable");
    }

    return PrimitiveType.BOOLEAN;
  }

  @Override
  public MJType visit(PlusExpression n, TypecheckContext typecheckContext) {
    PrimaryExpression leftExpression = n.f0;
    PrimaryExpression rightExpression = n.f2;

    MJType leftType = leftExpression.accept(this, typecheckContext);
    MJType rightType = rightExpression.accept(this, typecheckContext);

    if (!leftType.equals(PrimitiveType.INT) || !rightType.equals(PrimitiveType.INT)) {
      System.err.println("Type mismatch in plus expression. Left type: " + leftType + ", right type: " + rightType);
      OutputMessage.outputErrorAndExit();
      throw new AssertionError("Unreachable");
    }

    return PrimitiveType.INT;
  }

  @Override
  public MJType visit(MinusExpression n, TypecheckContext typecheckContext) {
    PrimaryExpression leftExpression = n.f0;
    PrimaryExpression rightExpression = n.f2;

    MJType leftType = leftExpression.accept(this, typecheckContext);
    MJType rightType = rightExpression.accept(this, typecheckContext);

    if (!leftType.equals(PrimitiveType.INT) || !rightType.equals(PrimitiveType.INT)) {
      System.err.println("Type mismatch in minus expression. Left type: " + leftType + ", right type: " + rightType);
      OutputMessage.outputErrorAndExit();
      throw new AssertionError("Unreachable");
    }

    return PrimitiveType.INT;
  }

  @Override
  public MJType visit(TimesExpression n, TypecheckContext typecheckContext) {
    PrimaryExpression leftExpression = n.f0;
    PrimaryExpression rightExpression = n.f2;

    MJType leftType = leftExpression.accept(this, typecheckContext);
    MJType rightType = rightExpression.accept(this, typecheckContext);

    if (!leftType.equals(PrimitiveType.INT) || !rightType.equals(PrimitiveType.INT)) {
      System.err.println("Type mismatch in times expression. Left type: " + leftType + ", right type: " + rightType);
      OutputMessage.outputErrorAndExit();
      throw new AssertionError("Unreachable");
    }

    return PrimitiveType.INT;
  }

  @Override
  public MJType visit(ArrayLookup n, TypecheckContext typecheckContext) {
    PrimaryExpression arrayExpression = n.f0;
    PrimaryExpression indexExpression = n.f2;

    MJType arrayType = arrayExpression.accept(this, typecheckContext);
    MJType indexType = indexExpression.accept(this, typecheckContext);

    if (!arrayType.equals(PrimitiveType.INT_ARRAY) || !indexType.equals(PrimitiveType.INT)) {
      System.err.println("Type mismatch in array lookup. Array type: " + arrayType + ", index type: " + indexType);
      OutputMessage.outputErrorAndExit();
      throw new AssertionError("Unreachable");
    }

    return PrimitiveType.INT;
  }

  @Override
  public MJType visit(ArrayLength n, TypecheckContext typecheckContext) {
    PrimaryExpression arrayExpression = n.f0;

    MJType arrayType = arrayExpression.accept(this, typecheckContext);

    if (!arrayType.equals(PrimitiveType.INT_ARRAY)) {
      System.err.println("Type mismatch in array length. Array type: " + arrayType);
      OutputMessage.outputErrorAndExit();
      throw new AssertionError("Unreachable");
    }

    return PrimitiveType.INT;
  }

  @Override
  public MJType visit(MessageSend n, TypecheckContext typecheckContext) {
    PrimaryExpression object = n.f0;
    String methodName = n.f2.f0.toString();
    NodeOptional argumentList = n.f4;
    SymbolTable symbolTable = typecheckContext.getSymbolTable();

    MJType objectType = object.accept(this, typecheckContext);
    if (!(objectType instanceof ClassType)) {
      System.err.println("Type mismatch in message send. Object type: " + objectType);
      OutputMessage.outputErrorAndExit();
      throw new AssertionError("Unreachable");
    }

    ClassInfo objectClassInfo = symbolTable.getClassInfo(((ClassType) objectType).getName());
    MethodInfo objectMethodInfo = objectClassInfo.getMethodInfo(methodName);

    MJType returnType = objectMethodInfo.getReturnType();
    List<MJType> parameterTypes = objectMethodInfo.getParameterTypes();

    List<MJType> argumentTypes = new ArrayList<>();
    if (argumentList.present()) {
      ExpressionList exprList = (ExpressionList) argumentList.node;

      MJType firstArg = exprList.f0.accept(this, typecheckContext);
      argumentTypes.add(firstArg);

      for (Node node : exprList.f1.nodes) {
        ExpressionRest rest = (ExpressionRest) node;
        MJType arg = rest.f1.accept(this, typecheckContext);
        argumentTypes.add(arg);
      }
    }

    if (parameterTypes.size() != argumentTypes.size()) {
      System.err.println("Argument count mismatch in message send...");
      OutputMessage.outputErrorAndExit();
      throw new AssertionError("Unreachable");
    }

    for (int i = 0; i < parameterTypes.size(); i++) {
      MJType expected = parameterTypes.get(i);
      MJType actual = argumentTypes.get(i);

      if (!actual.equals(expected)) { // TODO: add subtype check
        System.err.println("Argument type mismatch at index " + i +
            " in method '" + methodName + "': expected " + expected + ", got " + actual);
        OutputMessage.outputErrorAndExit();
        throw new AssertionError("Unreachable");
      }
    }

    return returnType;
  }

  @Override
  public MJType visit(PrimaryExpression n, TypecheckContext typecheckContext) {
    return n.f0.accept(this, typecheckContext);
  }

  @Override
  public MJType visit(IntegerLiteral n, TypecheckContext typecheckContext) {
    return PrimitiveType.INT;
  }

  @Override
  public MJType visit(TrueLiteral n, TypecheckContext typecheckContext) {
    return PrimitiveType.BOOLEAN;
  }

  @Override
  public MJType visit(FalseLiteral n, TypecheckContext typecheckContext) {
    return PrimitiveType.BOOLEAN;
  }

  @Override
  public MJType visit(Identifier n, TypecheckContext typecheckContext) {
    String identifierName = n.f0.toString();
    ClassInfo classInfo = typecheckContext.getCurrentClassInfo();
    MethodInfo methodInfo = typecheckContext.getCurrentMethodInfo();

    MJType type = null;

    type = methodInfo.getLocalVariableType(identifierName); // TODO: Maybe add null check

    if (type != null) {
      return type;
    }

    type = methodInfo.getParameterType(identifierName);

    if (type != null) {
      return type;
    }

    type = classInfo.getFieldType(identifierName);

    if (type != null) {
      return type;
    }

    System.err.println("Identifier " + identifierName + " not found in method " + methodInfo.getMethodName()
        + " and class " + classInfo.getClassName());
    OutputMessage.outputErrorAndExit();
    throw new AssertionError("Unreachable");
  }

  @Override
  public MJType visit(ThisExpression n, TypecheckContext typecheckContext) {
    ClassInfo classInfo = typecheckContext.getCurrentClassInfo();
    String className = classInfo.getClassName();
    ClassType classType = new ClassType(className);

    return classType;
  }

  @Override
  public MJType visit(ArrayAllocationExpression n, TypecheckContext typecheckContext) {
    MJType expressionType = n.f3.accept(this, typecheckContext);

    if (!expressionType.equals(PrimitiveType.INT)) {
      System.err.println("Type mismatch in array allocation expression. Expression type: " + expressionType);
      OutputMessage.outputErrorAndExit();
      throw new AssertionError("Unreachable");
    }

    return PrimitiveType.INT_ARRAY;
  }

  @Override
  public MJType visit(AllocationExpression n, TypecheckContext typecheckContext) {
    String className = n.f1.f0.toString();
    SymbolTable symbolTable = typecheckContext.getSymbolTable();

    symbolTable.getClassInfo(className);
    ClassType classType = new ClassType(className);

    return classType;
  }

  @Override
  public MJType visit(NotExpression n, TypecheckContext typecheckContext) {
    MJType expressionType = n.f1.accept(this, typecheckContext);

    if (!expressionType.equals(PrimitiveType.BOOLEAN)) {
      System.err.println("Type mismatch in not expression. Expression type: " + expressionType);
      OutputMessage.outputErrorAndExit();
      throw new AssertionError("Unreachable");
    }

    return PrimitiveType.BOOLEAN;
  }

  @Override
  public MJType visit(BracketExpression n, TypecheckContext typecheckContext) {
    MJType expressionType = n.f1.accept(this, typecheckContext);

    return expressionType;
  }
}
