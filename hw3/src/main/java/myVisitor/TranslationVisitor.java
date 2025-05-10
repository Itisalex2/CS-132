package myVisitor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import IR.token.FunctionName;
import IR.token.Identifier;
import IR.token.Label;
import context.TranslationContext;
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
import minijava.syntaxtree.FormalParameter;
import minijava.syntaxtree.Goal;
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
import minijava.syntaxtree.VarDeclaration;
import minijava.syntaxtree.WhileStatement;
import minijava.visitor.GJDepthFirst;
import model.SparrowResult;
import model.TranslationResult;
import sparrow.Add;
import sparrow.Alloc;
import sparrow.Block;
import sparrow.Call;
import sparrow.ErrorMessage;
import sparrow.FunctionDecl;
import sparrow.Goto;
import sparrow.IfGoto;
import sparrow.Instruction;
import sparrow.LabelInstr;
import sparrow.LessThan;
import sparrow.Load;
import sparrow.Move_Id_FuncName;
import sparrow.Move_Id_Id;
import sparrow.Move_Id_Integer;
import sparrow.Multiply;
import sparrow.Print;
import sparrow.Program;
import sparrow.Store;
import sparrow.Subtract;
import symbolTable.ClassInfo;
import symbolTable.MethodInfo;
import symbolTable.SymbolTable;
import type.ClassType;
import type.MJType;

public class TranslationVisitor extends GJDepthFirst<TranslationResult, TranslationContext> {
  private Program prog;

  public Program getProgram() {
    return prog;
  }

  @Override
  public TranslationResult visit(Goal n, TranslationContext context) {
    MainClass mainClass = n.f0;
    NodeListOptional classes = n.f1;

    TranslationResult mainClassTR = mainClass.accept(this, context);
    List<FunctionDecl> mainClassFunction = mainClassTR.getFunctions();

    List<FunctionDecl> classFunctions = new ArrayList<>();
    for (Node classNode : classes.nodes) {
      TranslationResult classTR = classNode.accept(this, context);
      classFunctions.addAll(classTR.getFunctions());
    }

    List<FunctionDecl> allFunctions = new ArrayList<>();
    allFunctions.addAll(mainClassFunction);
    allFunctions.addAll(classFunctions);

    prog = new Program(allFunctions);
    return TranslationResult.ofProgram(prog);
  }

  @Override
  public TranslationResult visit(MainClass n, TranslationContext context) {
    SymbolTable symbolTable = context.getSymbolTable();
    String className = n.f1.f0.toString();
    NodeListOptional varDeclarations = n.f14;
    NodeListOptional statements = n.f15;

    ClassInfo classInfo = symbolTable.getClassInfo(className);
    MethodInfo mainMethodInfo = classInfo.getMainMethodInfo();
    TranslationContext newContext = new TranslationContext(symbolTable, classInfo, mainMethodInfo);

    List<Instruction> instructions = new ArrayList<>();

    for (Node varDeclNode : varDeclarations.nodes) {
      varDeclNode.accept(this, newContext);
    }

    for (Node statementNode : statements.nodes) {
      TranslationResult statementTR = statementNode.accept(this, newContext);
      instructions.addAll(statementTR.getSparrowResult().getInstructions());
    }

    Identifier ret = newContext.getNextVariable();
    instructions.add(new Move_Id_Integer(ret, 0));

    Block block = new Block(instructions, ret);
    FunctionName fname = new FunctionName("main");
    FunctionDecl f = new FunctionDecl(fname, new ArrayList<>(), block);

    ArrayList<FunctionDecl> funcs = new ArrayList<>();
    funcs.add(f);

    return TranslationResult.ofFunctions(funcs);
  }

  @Override
  public TranslationResult visit(TypeDeclaration n, TranslationContext context) {
    return n.f0.accept(this, context);
  }

  @Override
  public TranslationResult visit(ClassDeclaration n, TranslationContext context) {
    SymbolTable symbolTable = context.getSymbolTable();
    String className = n.f1.f0.toString();
    NodeListOptional methodDeclarations = n.f4;

    ClassInfo classInfo = symbolTable.getClassInfo(className);
    TranslationContext newContext = new TranslationContext(symbolTable, classInfo, null);

    List<FunctionDecl> functions = new ArrayList<>();
    for (Node methodDeclNode : methodDeclarations.nodes) {
      TranslationResult methodTR = methodDeclNode.accept(this, newContext);
      functions.addAll(methodTR.getFunctions());
    }

    return TranslationResult.ofFunctions(functions);
  }

  @Override
  public TranslationResult visit(ClassExtendsDeclaration n, TranslationContext context) {
    SymbolTable symbolTable = context.getSymbolTable();
    String className = n.f1.f0.toString();
    NodeListOptional methodDeclarations = n.f6;

    ClassInfo classInfo = symbolTable.getClassInfo(className);
    TranslationContext newContext = new TranslationContext(symbolTable, classInfo, null);

    List<FunctionDecl> functions = new ArrayList<>();
    for (Node methodDeclNode : methodDeclarations.nodes) {
      TranslationResult methodTR = methodDeclNode.accept(this, newContext);
      functions.addAll(methodTR.getFunctions());
    }

    return TranslationResult.ofFunctions(functions);
  }

  /**
   * Updates the context local variable mapping
   **/
  @Override
  public TranslationResult visit(VarDeclaration n, TranslationContext context) {
    String name = n.f1.f0.toString();

    if (context.getCurrentMethodInfo() != null) {
      context.addLocalVar(name);
    } // Class fields are put into the heap during symbol table construction

    return null;
  }

  @Override
  public TranslationResult visit(MethodDeclaration n, TranslationContext context) {
    String methodName = n.f2.f0.toString();
    SymbolTable symbolTable = context.getSymbolTable();
    ClassInfo classInfo = context.getCurrentClassInfo();
    MethodInfo methodInfo = classInfo.getMethodInfo(methodName);
    NodeOptional formalParameters = n.f4;
    NodeListOptional varDeclarations = n.f7;
    NodeListOptional statements = n.f8;
    Expression returnExpression = n.f10;

    TranslationContext newContext = new TranslationContext(symbolTable, classInfo, methodInfo);

    newContext.addLocalVar("this");
    formalParameters.accept(this, newContext);
    varDeclarations.accept(this, newContext);

    List<Instruction> instructions = new ArrayList<>();
    for (Node statementNode : statements.nodes) {
      TranslationResult statementTR = statementNode.accept(this, newContext);
      instructions.addAll(statementTR.getSparrowResult().getInstructions());
    }

    SparrowResult returnExpressionSparrowResult = returnExpression.accept(this, newContext).getSparrowResult();
    instructions.addAll(returnExpressionSparrowResult.getInstructions());

    Block block = new Block(instructions, returnExpressionSparrowResult.getResult());
    FunctionName fname = getFunctionName(classInfo, methodInfo);

    List<Identifier> formalParametersIdentifiers = new ArrayList<>();
    formalParametersIdentifiers.add(newContext.lookupVar("this"));

    for (String paramName : methodInfo.getParameterNames()) {
      Identifier id = newContext.lookupVar(paramName);
      formalParametersIdentifiers.add(id);
    }

    FunctionDecl f = new FunctionDecl(fname, formalParametersIdentifiers, block);
    ArrayList<FunctionDecl> funcs = new ArrayList<>();
    funcs.add(f);

    return TranslationResult.ofFunctions(funcs);
  }

  @Override
  public TranslationResult visit(FormalParameter n, TranslationContext context) {
    String name = n.f1.f0.toString();
    context.addLocalVar(name);

    return null;
  }

  @Override
  public TranslationResult visit(Statement n, TranslationContext context) {
    return n.f0.accept(this, context);
  }

  @Override
  public TranslationResult visit(minijava.syntaxtree.Block n, TranslationContext context) {
    NodeListOptional statements = n.f1;
    List<Instruction> instructions = new ArrayList<>();

    for (Node statementNode : statements.nodes) {
      TranslationResult statementTR = statementNode.accept(this, context);
      instructions.addAll(statementTR.getSparrowResult().getInstructions());
    }

    return TranslationResult.ofSparrowResult(new SparrowResult(instructions, null));
  }

  @Override
  public TranslationResult visit(AssignmentStatement n, TranslationContext context) {
    String name = n.f0.f0.toString();
    Expression expression = n.f2;
    SparrowResult expressionSR = expression.accept(this, context).getSparrowResult();

    List<Instruction> instructions = new ArrayList<>(expressionSR.getInstructions());

    if (context.hasVar(name)) {
      Identifier id = context.lookupVar(name);
      instructions.add(new Move_Id_Id(id, expressionSR.getResult()));
    } else {
      Identifier id = context.lookupVar("this");
      ClassInfo classInfo = context.getCurrentClassInfo();
      int idFieldOffset = classInfo.getFieldOffset(name);

      instructions.add(new Store(id, idFieldOffset, expressionSR.getResult()));
    }

    return TranslationResult.ofSparrowResult(new SparrowResult(instructions, null));
  }

  @Override
  public TranslationResult visit(ArrayAssignmentStatement n, TranslationContext context) {
    SparrowResult arraySR = n.f0.accept(this, context).getSparrowResult();
    Identifier arrayPtr = arraySR.getResult();

    Expression indexExpression = n.f2;
    Expression valueExpression = n.f5;

    List<Instruction> instructions = new ArrayList<>(arraySR.getInstructions());

    SparrowResult indexSR = indexExpression.accept(this, context).getSparrowResult();
    Identifier index = indexSR.getResult();
    instructions.addAll(indexSR.getInstructions());

    SparrowResult valueSR = valueExpression.accept(this, context).getSparrowResult();
    Identifier value = valueSR.getResult();
    instructions.addAll(valueSR.getInstructions());

    Identifier zero = context.getNextVariable();
    Identifier one = context.getNextVariable();
    Identifier negativeOne = context.getNextVariable();
    Identifier four = context.getNextVariable();
    Identifier arraySize = context.getNextVariable();
    Identifier indexIsNonNegative = context.getNextVariable();
    Identifier indexIsLessThanArraySize = context.getNextVariable();
    Identifier indexInBounds = context.getNextVariable();
    Identifier indexBytePos = context.getNextVariable();
    Identifier arrayIndexAddress = context.getNextVariable();
    Label outOfBoundsLabel = TranslationContext.getNextUniqueLabel("ArrayIndexOutOfBoundsException");
    Label inBoundsLabel = TranslationContext.getNextUniqueLabel("inBounds");

    instructions.add(new Move_Id_Integer(zero, 0));
    instructions.add(new Move_Id_Integer(one, 1));
    instructions.add(new Subtract(negativeOne, zero, one));
    instructions.add(new Move_Id_Integer(four, 4));
    instructions.add(new Load(arraySize, arrayPtr, 0));
    instructions.add(new LessThan(indexIsNonNegative, negativeOne, index));
    instructions.add(new LessThan(indexIsLessThanArraySize, index, arraySize));
    instructions.add(new Multiply(indexInBounds, indexIsNonNegative, indexIsLessThanArraySize));
    instructions.add(new IfGoto(indexInBounds, outOfBoundsLabel));
    instructions.add(new Goto(inBoundsLabel));
    instructions.add(new LabelInstr(outOfBoundsLabel));
    instructions.add(new ErrorMessage(constant.ErrorMessage.arrayIndexOutOfBounds));
    instructions.add(new LabelInstr(inBoundsLabel));
    instructions.add(new Multiply(indexBytePos, index, four));
    instructions.add(new Add(indexBytePos, indexBytePos, four));
    instructions.add(new Add(arrayIndexAddress, arrayPtr, indexBytePos));
    instructions.add(new Store(arrayIndexAddress, 0, value));

    return TranslationResult.ofSparrowResult(new SparrowResult(instructions, null));
  }

  @Override
  public TranslationResult visit(IfStatement n, TranslationContext context) {
    Expression condition = n.f2;
    Statement thenStatement = n.f4;
    Statement elseStatement = n.f6;

    SparrowResult conditionSR = condition.accept(this, context).getSparrowResult();
    Identifier conditionResult = conditionSR.getResult();
    SparrowResult thenSR = thenStatement.accept(this, context).getSparrowResult();
    SparrowResult elseSR = elseStatement.accept(this, context).getSparrowResult();

    List<Instruction> instructions = new ArrayList<>();
    instructions.addAll(conditionSR.getInstructions());

    Label elseLabel = TranslationContext.getNextUniqueLabel("else");
    Label endLabel = TranslationContext.getNextUniqueLabel("end");

    instructions.add(new IfGoto(conditionResult, elseLabel));
    instructions.addAll(thenSR.getInstructions());
    instructions.add(new Goto(endLabel));
    instructions.add(new LabelInstr(elseLabel));
    instructions.addAll(elseSR.getInstructions());
    instructions.add(new LabelInstr(endLabel));

    return TranslationResult.ofSparrowResult(new SparrowResult(instructions, null));
  }

  @Override
  public TranslationResult visit(WhileStatement n, TranslationContext context) {
    Expression condition = n.f2;
    Statement bodyStatement = n.f4;

    SparrowResult conditionSR = condition.accept(this, context).getSparrowResult();
    Identifier conditionResult = conditionSR.getResult();
    SparrowResult bodySR = bodyStatement.accept(this, context).getSparrowResult();

    List<Instruction> instructions = new ArrayList<>();

    Label startLabel = TranslationContext.getNextUniqueLabel("whileStart");
    Label endLabel = TranslationContext.getNextUniqueLabel("whileEnd");

    instructions.add(new LabelInstr(startLabel));
    instructions.addAll(conditionSR.getInstructions());
    instructions.add(new IfGoto(conditionResult, endLabel));
    instructions.addAll(bodySR.getInstructions());
    instructions.add(new Goto(startLabel));
    instructions.add(new LabelInstr(endLabel));

    return TranslationResult.ofSparrowResult(new SparrowResult(instructions, null));
  }

  @Override
  public TranslationResult visit(PrintStatement n, TranslationContext context) {
    Expression expression = n.f2;

    SparrowResult expressionSR = expression.accept(this, context).getSparrowResult();
    Identifier expressionResult = expressionSR.getResult();
    List<Instruction> instructions = expressionSR.getInstructions();

    Instruction printInstruction = new Print(expressionResult);
    instructions.add(printInstruction);

    return TranslationResult.ofSparrowResult(new SparrowResult(instructions, null));
  }

  @Override
  public TranslationResult visit(Expression n, TranslationContext context) {
    return n.f0.accept(this, context);
  }

  @Override
  public TranslationResult visit(AndExpression n, TranslationContext context) {
    PrimaryExpression leftExpression = n.f0;
    PrimaryExpression rightExpression = n.f2;

    SparrowResult leftSR = leftExpression.accept(this, context).getSparrowResult();
    SparrowResult rightSR = rightExpression.accept(this, context).getSparrowResult();

    Identifier zero = context.getNextVariable();
    Identifier result = context.getNextVariable();

    Label falseLabel = TranslationContext.getNextUniqueLabel("andFalse");
    Label endLabel = TranslationContext.getNextUniqueLabel("andEnd");

    List<Instruction> instructions = new ArrayList<>();
    instructions.add(new Move_Id_Integer(zero, 0));
    instructions.addAll(leftSR.getInstructions());
    instructions.add(new IfGoto(leftSR.getResult(), falseLabel));
    instructions.addAll(rightSR.getInstructions());
    instructions.add(new IfGoto(rightSR.getResult(), falseLabel));
    instructions.add(new Move_Id_Integer(result, 1));
    instructions.add(new Goto(endLabel));
    instructions.add(new LabelInstr(falseLabel));
    instructions.add(new Move_Id_Integer(result, 0));
    instructions.add(new LabelInstr(endLabel));

    return TranslationResult.ofSparrowResult(new SparrowResult(instructions, result));
  }

  @Override
  public TranslationResult visit(CompareExpression n, TranslationContext context) {
    PrimaryExpression leftExpression = n.f0;
    PrimaryExpression rightExpression = n.f2;

    SparrowResult leftSR = leftExpression.accept(this, context).getSparrowResult();
    SparrowResult rightSR = rightExpression.accept(this, context).getSparrowResult();
    Identifier result = context.getNextVariable();

    List<Instruction> instructions = new ArrayList<>();
    instructions.addAll(leftSR.getInstructions());
    instructions.addAll(rightSR.getInstructions());
    instructions.add(new LessThan(result, leftSR.getResult(), rightSR.getResult()));

    return TranslationResult.ofSparrowResult(new SparrowResult(instructions, result));
  }

  @Override
  public TranslationResult visit(PlusExpression n, TranslationContext context) {
    PrimaryExpression leftExpression = n.f0;
    PrimaryExpression rightExpression = n.f2;

    SparrowResult leftSR = leftExpression.accept(this, context).getSparrowResult();
    SparrowResult rightSR = rightExpression.accept(this, context).getSparrowResult();

    Identifier id = context.getNextVariable();
    Instruction addInstruction = new Add(id, leftSR.getResult(), rightSR.getResult());

    List<Instruction> instructions = new ArrayList<>();
    instructions.addAll(leftSR.getInstructions());
    instructions.addAll(rightSR.getInstructions());
    instructions.add(addInstruction);
    SparrowResult sparrowResult = new SparrowResult(instructions, id);

    return TranslationResult.ofSparrowResult(sparrowResult);
  }

  @Override
  public TranslationResult visit(MinusExpression n, TranslationContext context) {
    PrimaryExpression leftExpression = n.f0;
    PrimaryExpression rightExpression = n.f2;

    SparrowResult leftSR = leftExpression.accept(this, context).getSparrowResult();
    SparrowResult rightSR = rightExpression.accept(this, context).getSparrowResult();

    Identifier id = context.getNextVariable();
    Instruction minusInstruction = new Subtract(id, leftSR.getResult(), rightSR.getResult());

    List<Instruction> instructions = new ArrayList<>();
    instructions.addAll(leftSR.getInstructions());
    instructions.addAll(rightSR.getInstructions());
    instructions.add(minusInstruction);
    SparrowResult sparrowResult = new SparrowResult(instructions, id);

    return TranslationResult.ofSparrowResult(sparrowResult);
  }

  @Override
  public TranslationResult visit(TimesExpression n, TranslationContext context) {
    PrimaryExpression leftExpression = n.f0;
    PrimaryExpression rightExpression = n.f2;

    SparrowResult leftSR = leftExpression.accept(this, context).getSparrowResult();
    SparrowResult rightSR = rightExpression.accept(this, context).getSparrowResult();

    Identifier id = context.getNextVariable();
    Instruction timesInstruction = new Multiply(id, leftSR.getResult(), rightSR.getResult());

    List<Instruction> instructions = new ArrayList<>();
    instructions.addAll(leftSR.getInstructions());
    instructions.addAll(rightSR.getInstructions());
    instructions.add(timesInstruction);
    SparrowResult sparrowResult = new SparrowResult(instructions, id);

    return TranslationResult.ofSparrowResult(sparrowResult);
  }

  @Override
  public TranslationResult visit(ArrayLookup n, TranslationContext context) {
    SparrowResult arraySR = n.f0.accept(this, context).getSparrowResult();
    Identifier arrayPtr = arraySR.getResult();
    PrimaryExpression indexExpression = n.f2;

    List<Instruction> instructions = new ArrayList<>(arraySR.getInstructions());

    SparrowResult indexSR = indexExpression.accept(this, context).getSparrowResult();
    Identifier index = indexSR.getResult();
    instructions.addAll(indexSR.getInstructions());

    Identifier zero = context.getNextVariable();
    Identifier one = context.getNextVariable();
    Identifier negativeOne = context.getNextVariable();
    Identifier four = context.getNextVariable();
    Identifier arraySize = context.getNextVariable();
    Identifier indexIsNonNegative = context.getNextVariable();
    Identifier indexIsLessThanArraySize = context.getNextVariable();
    Identifier indexInBounds = context.getNextVariable();
    Identifier indexBytePos = context.getNextVariable();
    Identifier arrayIndexAddress = context.getNextVariable();
    Identifier result = context.getNextVariable();
    Label outOfBoundsLabel = TranslationContext.getNextUniqueLabel("ArrayIndexOutOfBoundsException");
    Label inBoundsLabel = TranslationContext.getNextUniqueLabel("inBounds");

    instructions.add(new Move_Id_Integer(zero, 0));
    instructions.add(new Move_Id_Integer(one, 1));
    instructions.add(new Subtract(negativeOne, zero, one));
    instructions.add(new Move_Id_Integer(four, 4));
    instructions.add(new Load(arraySize, arrayPtr, 0));
    instructions.add(new LessThan(indexIsNonNegative, negativeOne, index));
    instructions.add(new LessThan(indexIsLessThanArraySize, index, arraySize));
    instructions.add(new Multiply(indexInBounds, indexIsNonNegative, indexIsLessThanArraySize));
    instructions.add(new IfGoto(indexInBounds, outOfBoundsLabel));
    instructions.add(new Goto(inBoundsLabel));
    instructions.add(new LabelInstr(outOfBoundsLabel));
    instructions.add(new ErrorMessage(constant.ErrorMessage.arrayIndexOutOfBounds));
    instructions.add(new LabelInstr(inBoundsLabel));
    instructions.add(new Multiply(indexBytePos, index, four));
    instructions.add(new Add(indexBytePos, indexBytePos, four));
    instructions.add(new Add(arrayIndexAddress, arrayPtr, indexBytePos));
    instructions.add(new Load(result, arrayIndexAddress, 0));

    return TranslationResult.ofSparrowResult(new SparrowResult(instructions, result));
  }

  @Override
  public TranslationResult visit(ArrayLength n, TranslationContext context) {
    PrimaryExpression array = n.f0;

    SparrowResult arraySR = array.accept(this, context).getSparrowResult();
    Identifier arrayPtr = arraySR.getResult();

    Identifier result = context.getNextVariable();

    List<Instruction> instructions = new ArrayList<>(arraySR.getInstructions());
    instructions.add(new Load(result, arrayPtr, 0));

    return TranslationResult.ofSparrowResult(new SparrowResult(instructions, result));
  }

  @Override
  public TranslationResult visit(MessageSend n, TranslationContext context) {
    PrimaryExpression object = n.f0;
    String methodName = n.f2.f0.toString();
    NodeOptional arguments = n.f4;
    SymbolTable symbolTable = context.getSymbolTable();
    MethodInfo methodInfo = context.getCurrentMethodInfo();

    SparrowResult objectSR = object.accept(this, context).getSparrowResult();
    Identifier objectId = objectSR.getResult();

    List<Instruction> instructions = new ArrayList<>();
    instructions.addAll(objectSR.getInstructions());

    List<Identifier> argIds = new ArrayList<>();

    if (arguments.present()) {
      ExpressionList args = (ExpressionList) arguments.node;
      Expression firstArg = args.f0;
      NodeListOptional restArgs = args.f1;

      SparrowResult firstArgSR = firstArg.accept(this, context).getSparrowResult();
      argIds.add(firstArgSR.getResult());
      instructions.addAll(firstArgSR.getInstructions());

      for (Node argNode : restArgs.nodes) {
        SparrowResult argSR = argNode.accept(this, context).getSparrowResult();
        argIds.add(argSR.getResult());
        instructions.addAll(argSR.getInstructions());
      }
    }

    List<Identifier> args = new ArrayList<>();
    args.add(objectId);
    args.addAll(argIds);

    ClassType objType = (ClassType) objectSR.getType();
    ClassInfo classInfo = symbolTable.getClassInfo(objType.getName());
    int methodOffset = classInfo.getMethodOffset(methodName);

    Identifier vmtPtr = context.getNextVariable();
    instructions.add(new Load(vmtPtr, objectId, 0));

    Identifier methodPtr = context.getNextVariable();
    instructions.add(new Load(methodPtr, vmtPtr, methodOffset));

    Identifier result = context.getNextVariable();
    instructions.add(new Call(result, methodPtr, args));

    MethodInfo calledMethod = classInfo.getMethodInfo(methodName);
    MJType returnType = calledMethod.getReturnType();

    return TranslationResult.ofSparrowResult(new SparrowResult(instructions, result).withType(returnType));
  }

  @Override
  public TranslationResult visit(ExpressionRest n, TranslationContext context) {
    return n.f1.accept(this, context);
  }

  @Override
  public TranslationResult visit(PrimaryExpression n, TranslationContext context) {
    return n.f0.accept(this, context);
  }

  @Override
  public TranslationResult visit(IntegerLiteral n, TranslationContext context) {
    int integer = Integer.parseInt(n.f0.toString());

    Identifier id = context.getNextVariable();
    Instruction moveInstruction = new Move_Id_Integer(id, integer);

    List<Instruction> instructions = new ArrayList<>(Arrays.asList(moveInstruction));
    SparrowResult sparrowResult = new SparrowResult(instructions, id);

    return TranslationResult.ofSparrowResult(sparrowResult);
  }

  @Override
  public TranslationResult visit(TrueLiteral n, TranslationContext context) {
    Identifier id = context.getNextVariable();
    Instruction moveInstruction = new Move_Id_Integer(id, 1);

    List<Instruction> instructions = new ArrayList<>(Arrays.asList(moveInstruction));
    SparrowResult sparrowResult = new SparrowResult(instructions, id);

    return TranslationResult.ofSparrowResult(sparrowResult);
  }

  @Override
  public TranslationResult visit(FalseLiteral n, TranslationContext context) {
    Identifier id = context.getNextVariable();
    Instruction moveInstruction = new Move_Id_Integer(id, 0);

    List<Instruction> instructions = new ArrayList<>(Arrays.asList(moveInstruction));
    SparrowResult sparrowResult = new SparrowResult(instructions, id);

    return TranslationResult.ofSparrowResult(sparrowResult);
  }

  @Override
  public TranslationResult visit(minijava.syntaxtree.Identifier n, TranslationContext context) {
    String name = n.f0.toString();
    Identifier id;

    List<Instruction> instructions = new ArrayList<>();
    if (context.hasVar(name)) {
      id = context.lookupVar(name);
      MJType type = context.getVarType(name);
      return TranslationResult.ofSparrowResult(new SparrowResult(instructions, id).withType(type));
    }

    Identifier thisId = context.lookupVar("this");
    ClassInfo classInfo = context.getCurrentClassInfo();
    int idFieldOffset = classInfo.getFieldOffset(name);

    id = context.getNextVariable();
    instructions.add(new Load(id, thisId, idFieldOffset));

    MJType fieldType = classInfo.getFieldType(name);

    return TranslationResult.ofSparrowResult(new SparrowResult(instructions, id).withType(fieldType));
  }

  @Override
  public TranslationResult visit(ThisExpression n, TranslationContext context) {
    Identifier thisId = context.lookupVar("this");
    ClassType classType = new ClassType(context.getCurrentClassInfo().getClassName());

    return TranslationResult.ofSparrowResult(new SparrowResult(new ArrayList<>(), thisId).withType(classType));
  }

  @Override
  public TranslationResult visit(ArrayAllocationExpression n, TranslationContext context) {
    Expression expression = n.f3;
    SparrowResult expressionSR = expression.accept(this, context).getSparrowResult();

    Identifier sizeId = expressionSR.getResult();
    Identifier arrayPtr = context.getNextVariable();
    Identifier one = context.getNextVariable();
    Identifier four = context.getNextVariable();
    Identifier sizeIdPlusOne = context.getNextVariable();

    List<Instruction> instructions = new ArrayList<>(expressionSR.getInstructions());

    instructions.add(new Move_Id_Integer(one, 1));
    instructions.add(new Move_Id_Integer(four, 4));
    instructions.add(new Add(sizeIdPlusOne, sizeId, one));
    instructions.add(new Multiply(sizeIdPlusOne, sizeIdPlusOne, four));
    instructions.add(new Alloc(arrayPtr, sizeIdPlusOne));
    instructions.add(new Store(arrayPtr, 0, sizeId));

    return TranslationResult.ofSparrowResult(new SparrowResult(instructions, arrayPtr));
  }

  @Override
  public TranslationResult visit(AllocationExpression n, TranslationContext context) {
    String className = n.f1.f0.toString();
    SymbolTable symbolTable = context.getSymbolTable();
    ClassInfo classInfo = symbolTable.getClassInfo(className);

    List<Instruction> instructions = new ArrayList<>();

    int fieldSize = classInfo.getFieldCount();
    int vmtSize = classInfo.getMethods().size();

    Identifier fieldsMemorySize = context.getNextVariable();
    Identifier fieldsTablePtr = context.getNextVariable();
    Identifier vmtMemorySize = context.getNextVariable();
    Identifier vmtPtr = context.getNextVariable();

    instructions.add(new Move_Id_Integer(fieldsMemorySize, (fieldSize) * 4));
    instructions.add(new Alloc(fieldsTablePtr, fieldsMemorySize));
    instructions.add(new Move_Id_Integer(vmtMemorySize, (vmtSize) * 4));
    instructions.add(new Alloc(vmtPtr, vmtMemorySize));

    List<String> methodNames = classInfo.getMethods().keySet().stream().collect(Collectors.toList());
    for (String methodName : methodNames) {
      int methodOffset = classInfo.getMethodOffset(methodName);
      Identifier functionPtr = context.getNextVariable();
      MethodInfo methodInfo = classInfo.getMethodInfo(methodName);
      instructions.add(new Move_Id_FuncName(functionPtr, getFunctionName(classInfo, methodInfo)));
      instructions.add(new Store(vmtPtr, methodOffset, functionPtr));
    }

    instructions.add(new Store(fieldsTablePtr, 0, vmtPtr));

    MJType classType = new ClassType(className);
    return TranslationResult.ofSparrowResult(new SparrowResult(instructions, fieldsTablePtr).withType(classType));
  }

  @Override
  public TranslationResult visit(NotExpression n, TranslationContext context) {
    Expression expression = n.f1;
    SparrowResult expressionSR = expression.accept(this, context).getSparrowResult();

    Identifier result = context.getNextVariable();
    Identifier one = context.getNextVariable();

    List<Instruction> instructions = new ArrayList<>(expressionSR.getInstructions());
    instructions.add(new Move_Id_Integer(one, 1));
    instructions.add(new Subtract(result, one, expressionSR.getResult()));

    return TranslationResult.ofSparrowResult(new SparrowResult(instructions, result));
  }

  @Override
  public TranslationResult visit(BracketExpression n, TranslationContext context) {
    return n.f1.accept(this, context);
  }

  private FunctionName getFunctionName(ClassInfo classInfo, MethodInfo methodInfo) {
    return new FunctionName(classInfo.getClassName() + '_' + methodInfo.getMethodName());
  }
}
