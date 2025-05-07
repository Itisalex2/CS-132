package myVisitor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import IR.token.FunctionName;
import IR.token.Identifier;
import context.TranslationContext;
import minijava.syntaxtree.ClassDeclaration;
import minijava.syntaxtree.ClassExtendsDeclaration;
import minijava.syntaxtree.Expression;
import minijava.syntaxtree.FalseLiteral;
import minijava.syntaxtree.Goal;
import minijava.syntaxtree.IntegerLiteral;
import minijava.syntaxtree.MainClass;
import minijava.syntaxtree.MethodDeclaration;
import minijava.syntaxtree.MinusExpression;
import minijava.syntaxtree.Node;
import minijava.syntaxtree.NodeListOptional;
import minijava.syntaxtree.PlusExpression;
import minijava.syntaxtree.PrimaryExpression;
import minijava.syntaxtree.PrintStatement;
import minijava.syntaxtree.Statement;
import minijava.syntaxtree.TimesExpression;
import minijava.syntaxtree.TrueLiteral;
import minijava.syntaxtree.TypeDeclaration;
import minijava.syntaxtree.VarDeclaration;
import minijava.visitor.GJDepthFirst;
import model.SparrowResult;
import model.TranslationResult;
import sparrow.Add;
import sparrow.Block;
import sparrow.FunctionDecl;
import sparrow.Instruction;
import sparrow.Move_Id_Integer;
import sparrow.Multiply;
import sparrow.Print;
import sparrow.Program;
import sparrow.Subtract;
import symbolTable.ClassInfo;
import symbolTable.MethodInfo;
import symbolTable.SymbolTable;

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
  public TranslationResult visit(TypeDeclaration n, TranslationContext typecheckContext) {
    return n.f0.accept(this, typecheckContext);
  }

  @Override
  public TranslationResult visit(ClassDeclaration n, TranslationContext typecheckContext) {
    throw new UnsupportedOperationException("ClassDeclaration not supported in TranslationVisitor");
  }

  @Override
  public TranslationResult visit(ClassExtendsDeclaration n, TranslationContext typecheckContext) {
    throw new UnsupportedOperationException("ClassExtendsDeclaration not supported in TranslationVisitor");
  }

  @Override
  public TranslationResult visit(MethodDeclaration n, TranslationContext typecheckContext) {
    throw new UnsupportedOperationException("MethodDeclaration not supported in TranslationVisitor");
  }

  @Override
  public TranslationResult visit(Statement n, TranslationContext typecheckContext) {
    return n.f0.accept(this, typecheckContext);
  }

  @Override
  public TranslationResult visit(PrintStatement n, TranslationContext typecheckContext) {
    Expression expression = n.f2;

    SparrowResult expressionTR = expression.accept(this, typecheckContext).getSparrowResult();
    Identifier expressionResult = expressionTR.getResult();
    List<Instruction> instructions = expressionTR.getInstructions();

    Instruction printInstruction = new Print(expressionResult);
    instructions.add(printInstruction);

    return TranslationResult.ofSparrowResult(new SparrowResult(instructions, null));
  }

  @Override
  public TranslationResult visit(Expression n, TranslationContext typecheckContext) {
    return n.f0.accept(this, typecheckContext);
  }

  @Override
  public TranslationResult visit(PrimaryExpression n, TranslationContext typecheckContext) {
    return n.f0.accept(this, typecheckContext);
  }

  @Override
  public TranslationResult visit(PlusExpression n, TranslationContext typecheckContext) {
    PrimaryExpression leftExpression = n.f0;
    PrimaryExpression rightExpression = n.f2;

    SparrowResult leftTR = leftExpression.accept(this, typecheckContext).getSparrowResult();
    SparrowResult rightTR = rightExpression.accept(this, typecheckContext).getSparrowResult();

    Identifier id = typecheckContext.getNextVariable();
    Instruction addInstruction = new Add(id, leftTR.getResult(), rightTR.getResult());

    List<Instruction> instructions = new ArrayList<>();
    instructions.addAll(leftTR.getInstructions());
    instructions.addAll(rightTR.getInstructions());
    instructions.add(addInstruction);
    SparrowResult sparrowResult = new SparrowResult(instructions, id);

    return TranslationResult.ofSparrowResult(sparrowResult);
  }

  @Override
  public TranslationResult visit(MinusExpression n, TranslationContext typecheckContext) {
    PrimaryExpression leftExpression = n.f0;
    PrimaryExpression rightExpression = n.f2;

    SparrowResult leftTR = leftExpression.accept(this, typecheckContext).getSparrowResult();
    SparrowResult rightTR = rightExpression.accept(this, typecheckContext).getSparrowResult();

    Identifier id = typecheckContext.getNextVariable();
    Instruction minusInstruction = new Subtract(id, leftTR.getResult(), rightTR.getResult());

    List<Instruction> instructions = new ArrayList<>();
    instructions.addAll(leftTR.getInstructions());
    instructions.addAll(rightTR.getInstructions());
    instructions.add(minusInstruction);
    SparrowResult sparrowResult = new SparrowResult(instructions, id);

    return TranslationResult.ofSparrowResult(sparrowResult);
  }

  @Override
  public TranslationResult visit(TimesExpression n, TranslationContext typecheckContext) {
    PrimaryExpression leftExpression = n.f0;
    PrimaryExpression rightExpression = n.f2;

    SparrowResult leftTR = leftExpression.accept(this, typecheckContext).getSparrowResult();
    SparrowResult rightTR = rightExpression.accept(this, typecheckContext).getSparrowResult();

    Identifier id = typecheckContext.getNextVariable();
    Instruction timesInstruction = new Multiply(id, leftTR.getResult(), rightTR.getResult());

    List<Instruction> instructions = new ArrayList<>();
    instructions.addAll(leftTR.getInstructions());
    instructions.addAll(rightTR.getInstructions());
    instructions.add(timesInstruction);
    SparrowResult sparrowResult = new SparrowResult(instructions, id);

    return TranslationResult.ofSparrowResult(sparrowResult);
  }

  @Override
  public TranslationResult visit(IntegerLiteral n, TranslationContext typecheckContext) {
    int integer = Integer.parseInt(n.f0.toString());

    Identifier id = typecheckContext.getNextVariable();
    Instruction moveInstruction = new Move_Id_Integer(id, integer);

    List<Instruction> instructions = new ArrayList<>(Arrays.asList(moveInstruction));
    SparrowResult sparrowResult = new SparrowResult(instructions, id);

    return TranslationResult.ofSparrowResult(sparrowResult);
  }

  @Override
  public TranslationResult visit(TrueLiteral n, TranslationContext typecheckContext) {
    Identifier id = typecheckContext.getNextVariable();
    Instruction moveInstruction = new Move_Id_Integer(id, 1);

    List<Instruction> instructions = new ArrayList<>(Arrays.asList(moveInstruction));
    SparrowResult sparrowResult = new SparrowResult(instructions, id);

    return TranslationResult.ofSparrowResult(sparrowResult);
  }

  @Override
  public TranslationResult visit(FalseLiteral n, TranslationContext typecheckContext) {
    Identifier id = typecheckContext.getNextVariable();
    Instruction moveInstruction = new Move_Id_Integer(id, 0);

    List<Instruction> instructions = new ArrayList<>(Arrays.asList(moveInstruction));
    SparrowResult sparrowResult = new SparrowResult(instructions, id);

    return TranslationResult.ofSparrowResult(sparrowResult);
  }
}
