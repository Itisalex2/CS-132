package visitor;

import java.util.ArrayList;
import java.util.List;

import IR.token.FunctionName;
import IR.token.Identifier;
import IR.token.Label;
import IR.token.Register;
import algorithm.LinearScanRegisterAllocator;
import model.TranslationResult;
import sparrow.visitor.ArgRetVisitor;
import sparrowv.Add;
import sparrowv.Alloc;
import sparrowv.Block;
import sparrowv.Call;
import sparrowv.ErrorMessage;
import sparrowv.FunctionDecl;
import sparrowv.Goto;
import sparrowv.IfGoto;
import sparrowv.Instruction;
import sparrowv.LabelInstr;
import sparrowv.LessThan;
import sparrowv.Load;
import sparrowv.Move_Id_Reg;
import sparrowv.Move_Reg_FuncName;
import sparrowv.Move_Reg_Id;
import sparrowv.Move_Reg_Integer;
import sparrowv.Multiply;
import sparrowv.Print;
import sparrowv.Program;
import sparrowv.Store;
import sparrowv.Subtract;

public class TranslationVisitor implements ArgRetVisitor<String, TranslationResult> {
  Program prog;
  LinearScanRegisterAllocator allocator;
  Register t0 = new Register("t0"); // Temp registers
  Register t1 = new Register("t1");

  public TranslationVisitor(LinearScanRegisterAllocator allocator) {
    this.allocator = allocator;
  }

  public Program getProgram() {
    return prog;
  }

  /* List<FunctionDecl> funDecls; */
  @Override
  public TranslationResult visit(sparrow.Program n, String arg) {
    List<sparrow.FunctionDecl> functionDecls = n.funDecls;

    List<FunctionDecl> translatedFunctionDecls = new ArrayList<>();
    for (sparrow.FunctionDecl functionDecl : functionDecls) {
      translatedFunctionDecls.add(functionDecl.accept(this, null).getFunctionDecl());
    }

    prog = new Program(translatedFunctionDecls);

    return TranslationResult.ofProgram(prog);
  }

  /*
   * Program parent;
   * FunctionName functionName;
   * List<Identifier> formalParameters;
   * Block block;
   */
  public TranslationResult visit(sparrow.FunctionDecl n, String arg) {
    FunctionName functionName = n.functionName;
    List<Identifier> formalParameters = n.formalParameters;
    sparrow.Block block = n.block;

    Block translatedBlock = block.accept(this, null).getBlock();
    FunctionDecl translatedFunctionDecl = new FunctionDecl(functionName, formalParameters, translatedBlock);

    return TranslationResult.ofFunctionDecl(translatedFunctionDecl);
  }

  /*
   * FunctionDecl parent;
   * List<Instruction> instructions;
   * Identifier return_id;
   */
  public TranslationResult visit(sparrow.Block n, String arg) {
    String functionName = n.parent.functionName.name;
    List<sparrow.Instruction> instructions = n.instructions;
    Identifier returnId = n.return_id;

    List<Instruction> translatedInstructions = new ArrayList<>();
    for (sparrow.Instruction instruction : instructions) {
      translatedInstructions.addAll(instruction.accept(this, functionName).getInstructions());
    }

    Block translatedBlock = new Block(translatedInstructions, returnId);
    return TranslationResult.ofBlock(translatedBlock);
  }

  /* Label label; */
  public TranslationResult visit(sparrow.LabelInstr n, String arg) {
    Label label = n.label;
    List<Instruction> instructions = new ArrayList<>();
    instructions.add(new LabelInstr(label));
    return TranslationResult.ofInstructions(instructions);
  }

  /*
   * Identifier lhs;
   * int rhs;
   */
  public TranslationResult visit(sparrow.Move_Id_Integer n, String arg) {
    Identifier lhs = n.lhs;
    int rhs = n.rhs;

    List<Instruction> instructions = new ArrayList<>();
    instructions.add(new Move_Reg_Integer(t0, rhs));
    instructions.add(new Move_Id_Reg(lhs, t0));

    return TranslationResult.ofInstructions(instructions);
  }

  /*
   * Identifier lhs;
   * FunctionName rhs;
   */
  public TranslationResult visit(sparrow.Move_Id_FuncName n, String arg) {
    Identifier lhs = n.lhs;
    FunctionName rhs = n.rhs;

    List<Instruction> instructions = new ArrayList<>();
    instructions.add(new Move_Reg_FuncName(t0, rhs));
    instructions.add(new Move_Id_Reg(lhs, t0));

    return TranslationResult.ofInstructions(instructions);
  }

  /*
   * Identifier lhs;
   * Identifier arg1;
   * Identifier arg2;
   */
  public TranslationResult visit(sparrow.Add n, String arg) {
    Identifier lhs = n.lhs;
    Identifier arg1 = n.arg1;
    Identifier arg2 = n.arg2;

    List<Instruction> instructions = new ArrayList<>();
    instructions.add(new Move_Reg_Id(t0, arg1));
    instructions.add(new Move_Reg_Id(t1, arg2));
    instructions.add(new Add(t0, t0, t1));
    instructions.add(new Move_Id_Reg(lhs, t0));

    return TranslationResult.ofInstructions(instructions);
  }

  /*
   * Identifier lhs;
   * Identifier arg1;
   * Identifier arg2;
   */
  public TranslationResult visit(sparrow.Subtract n, String arg) {
    Identifier lhs = n.lhs;
    Identifier arg1 = n.arg1;
    Identifier arg2 = n.arg2;

    List<Instruction> instructions = new ArrayList<>();
    instructions.add(new Move_Reg_Id(t0, arg1));
    instructions.add(new Move_Reg_Id(t1, arg2));
    instructions.add(new Subtract(t0, t0, t1));
    instructions.add(new Move_Id_Reg(lhs, t0));

    return TranslationResult.ofInstructions(instructions);
  }

  /*
   * Identifier lhs;
   * Identifier arg1;
   * Identifier arg2;
   */
  public TranslationResult visit(sparrow.Multiply n, String arg) {
    Identifier lhs = n.lhs;
    Identifier arg1 = n.arg1;
    Identifier arg2 = n.arg2;

    List<Instruction> instructions = new ArrayList<>();
    instructions.add(new Move_Reg_Id(t0, arg1));
    instructions.add(new Move_Reg_Id(t1, arg2));
    instructions.add(new Multiply(t0, t0, t1));
    instructions.add(new Move_Id_Reg(lhs, t0));

    return TranslationResult.ofInstructions(instructions);
  }

  /*
   * Identifier lhs;
   * Identifier arg1;
   * Identifier arg2;
   */
  public TranslationResult visit(sparrow.LessThan n, String arg) {
    Identifier lhs = n.lhs;
    Identifier arg1 = n.arg1;
    Identifier arg2 = n.arg2;

    List<Instruction> instructions = new ArrayList<>();
    instructions.add(new Move_Reg_Id(t0, arg1));
    instructions.add(new Move_Reg_Id(t1, arg2));
    instructions.add(new LessThan(t0, t0, t1));
    instructions.add(new Move_Id_Reg(lhs, t0));

    return TranslationResult.ofInstructions(instructions);
  }

  /*
   * Identifier lhs;
   * Identifier base;
   * int offset;
   */
  public TranslationResult visit(sparrow.Load n, String arg) {
    Identifier lhs = n.lhs;
    Identifier base = n.base;
    int offset = n.offset;

    List<Instruction> instructions = new ArrayList<>();
    instructions.add(new Move_Reg_Id(t0, base));
    instructions.add(new Load(t0, t0, offset));
    instructions.add(new Move_Id_Reg(lhs, t0));

    return TranslationResult.ofInstructions(instructions);
  }

  /*
   * Identifier base;
   * int offset;
   * Identifier rhs;
   */
  public TranslationResult visit(sparrow.Store n, String arg) {
    Identifier base = n.base;
    int offset = n.offset;
    Identifier rhs = n.rhs;

    List<Instruction> instructions = new ArrayList<>();
    instructions.add(new Move_Reg_Id(t0, base));
    instructions.add(new Move_Reg_Id(t1, rhs));
    instructions.add(new Store(t0, offset, t1));

    return TranslationResult.ofInstructions(instructions);
  }

  /*
   * Identifier lhs;
   * Identifier rhs;
   */
  public TranslationResult visit(sparrow.Move_Id_Id n, String arg) {
    Identifier lhs = n.lhs;
    Identifier rhs = n.rhs;

    List<Instruction> instructions = new ArrayList<>();
    instructions.add(new Move_Reg_Id(t0, rhs));
    instructions.add(new Move_Id_Reg(lhs, t0));

    return TranslationResult.ofInstructions(instructions);
  }

  /*
   * Identifier lhs;
   * Identifier size;
   */
  public TranslationResult visit(sparrow.Alloc n, String arg) {
    Identifier lhs = n.lhs;
    Identifier size = n.size;

    List<Instruction> instructions = new ArrayList<>();
    instructions.add(new Move_Reg_Id(t0, size));
    instructions.add(new Alloc(t0, t0));
    instructions.add(new Move_Id_Reg(lhs, t0));

    return TranslationResult.ofInstructions(instructions);
  }

  /* Identifier content; */
  public TranslationResult visit(sparrow.Print n, String arg) {
    Identifier content = n.content;

    List<Instruction> instructions = new ArrayList<>();
    instructions.add(new Move_Reg_Id(t0, content));
    instructions.add(new Print(t0));

    return TranslationResult.ofInstructions(instructions);
  }

  /* String msg; */
  public TranslationResult visit(sparrow.ErrorMessage n, String arg) {
    String msg = n.msg;

    List<Instruction> instructions = new ArrayList<>();
    instructions.add(new ErrorMessage(msg));

    return TranslationResult.ofInstructions(instructions);
  }

  /* Label label; */
  public TranslationResult visit(sparrow.Goto n, String arg) {
    Label label = n.label;

    List<Instruction> instructions = new ArrayList<>();
    instructions.add(new Goto(label));

    return TranslationResult.ofInstructions(instructions);
  }

  /*
   * Identifier condition;
   * Label label;
   */
  public TranslationResult visit(sparrow.IfGoto n, String arg) {
    Identifier condition = n.condition;
    Label label = n.label;

    List<Instruction> instructions = new ArrayList<>();
    instructions.add(new Move_Reg_Id(t0, condition));
    instructions.add(new IfGoto(t0, label));

    return TranslationResult.ofInstructions(instructions);
  }

  /*
   * Identifier lhs;
   * Identifier callee;
   * List<Identifier> args;
   */
  public TranslationResult visit(sparrow.Call n, String arg) {
    Identifier lhs = n.lhs;
    Identifier callee = n.callee;
    List<Identifier> args = n.args;

    List<Instruction> instructions = new ArrayList<>();
    instructions.add(new Move_Reg_Id(t0, callee));
    instructions.add(new Call(t0, t0, args));
    instructions.add(new Move_Id_Reg(lhs, t0));

    return TranslationResult.ofInstructions(instructions);
  }
}
