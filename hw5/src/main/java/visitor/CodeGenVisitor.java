package visitor;

import backend.Emitter;
import model.FunctionContext;
import model.TranslationContext;
import sparrowv.*;
import sparrowv.visitor.ArgVisitor;

public class CodeGenVisitor implements ArgVisitor<FunctionContext> {
  private final TranslationContext tc;
  private final Emitter out = new Emitter();

  public CodeGenVisitor(TranslationContext tc) {
    this.tc = tc;
  }

  public String generate(Program p) {
    p.accept(this, null);
    return out.toString();
  }

  /* List<FunctionDecl> funDecls; */
  @Override
  public void visit(Program n, FunctionContext _ignored) {
    for (FunctionDecl fd : n.funDecls) {
      FunctionContext ctx = tc.getFunctionContext(fd.functionName.toString());
      fd.accept(this, ctx);
      out.emit(""); // blank line between functions
    }
  }

  /*
   * Program parent;
   * FunctionName functionName;
   * List<Identifier> formalParameters;
   * Block block;
   */
  @Override
  public void visit(FunctionDecl n, FunctionContext ctx) {
    out.emit(".globl " + n.functionName);
    out.emit(n.functionName + ":");
    out.inc();

    emitPrologue(ctx);

    /* visit body */
    n.block.accept(this, ctx);

    emitEpilogue(ctx);
    out.dec();
  }

  /*
   * FunctionDecl parent;
   * List<Instruction> instructions;
   * Identifier return_id;
   */
  @Override
  public void visit(Block n, FunctionContext ctx) {
    for (Instruction i : n.instructions) {
      i.accept(this, ctx);
    }
  }

  /* Label label; */
  @Override
  public void visit(LabelInstr n, FunctionContext ctx) {
    // TODO
  }

  /*
   * Register lhs;
   * int rhs;
   */
  @Override
  public void visit(Move_Reg_Integer n, FunctionContext ctx) {
    out.emit("li   " + n.lhs + ", " + n.rhs);
  }

  /*
   * Register lhs;
   * FunctionName rhs;
   */
  @Override
  public void visit(Move_Reg_FuncName n, FunctionContext ctx) {
    out.emit("la   " + n.lhs + ", " + n.rhs);
  }

  /*
   * Register lhs;
   * Register arg1;
   * Register arg2;
   */
  @Override
  public void visit(Add n, FunctionContext ctx) {
    out.emit("add  " + n.lhs + ", " + n.arg1 + ", " + n.arg2);
  }

  /*
   * Register lhs;
   * Register arg1;
   * Register arg2;
   */
  @Override
  public void visit(Subtract n, FunctionContext ctx) {
    out.emit("sub  " + n.lhs + ", " + n.arg1 + ", " + n.arg2);
  }

  /*
   * Register lhs;
   * Register arg1;
   * Register arg2;
   */
  @Override
  public void visit(Multiply n, FunctionContext ctx) {
    out.emit("mul  " + n.lhs + ", " + n.arg1 + ", " + n.arg2);
  }

  /*
   * Register lhs;
   * Register arg1;
   * Register arg2;
   */
  @Override
  public void visit(LessThan n, FunctionContext ctx) {
    out.emit("slt  " + n.lhs + ", " + n.arg1 + ", " + n.arg2);
  }

  /*
   * Register lhs;
   * Register base;
   * int offset;
   */
  @Override
  public void visit(Load n, FunctionContext ctx) {
    out.emit("lw   " + n.lhs + ", " + n.offset + "(" + n.base + ")");
  }

  /*
   * Register base;
   * int offset;
   * Register rhs;
   */
  @Override
  public void visit(Store n, FunctionContext ctx) {
    out.emit("sw   " + n.rhs + ", " + n.offset + "(" + n.base + ")");
  }

  /*
   * Register lhs;
   * Register rhs;
   */
  @Override
  public void visit(Move_Reg_Reg n, FunctionContext ctx) {
    out.emit("mv   " + n.lhs + ", " + n.rhs);
  }

  /*
   * Identifier lhs;
   * Register rhs;
   */
  @Override
  public void visit(Move_Id_Reg n, FunctionContext ctx) {
    // TODO
  }

  /*
   * Register lhs;
   * Identifier rhs;
   */
  @Override
  public void visit(Move_Reg_Id n, FunctionContext ctx) {
    // TODO
  }

  /*
   * Register lhs;
   * Register size;
   */
  @Override
  public void visit(Alloc n, FunctionContext ctx) {
    out.emit("mv   a0, " + n.size);
    out.emit("jal  alloc");
    out.emit("mv   " + n.lhs + ", a0");
  }

  /* Register content; */
  @Override
  public void visit(Print n, FunctionContext ctx) {
    out.emit("mv   a0, " + n.content);
    out.emit("jal  print");
  }

  /* String msg; */
  @Override
  public void visit(ErrorMessage n, FunctionContext ctx) {
    out.emit("la   a0, " + n.msg);
    out.emit("jal  error");
  }

  /* Label label; */
  @Override
  public void visit(Goto n, FunctionContext ctx) {
    // TODO
  }

  /*
   * Register condition;
   * Label label;
   */
  @Override
  public void visit(IfGoto n, FunctionContext ctx) {
    // TODO
  }

  /*
   * Register lhs;
   * Register callee;
   * List<Identifier> args;
   */
  @Override
  public void visit(Call n, FunctionContext ctx) {
    // TODO
  }

  private void emitPrologue(FunctionContext ctx) { // TODO: double check
    int fSize = ctx.getFrameSize();
    out.emit("addi sp, sp, -" + fSize);
    out.emit("sw   ra, " + (fSize - 4) + "(sp)");
    out.emit("sw   fp, " + (fSize - 8) + "(sp)");
    out.emit("mv   fp, sp");
  }

  private void emitEpilogue(FunctionContext ctx) { // TODO: double check
    int fSize = ctx.getFrameSize();
    out.emit("lw   ra, " + (fSize - 4) + "(sp)");
    out.emit("lw   fp, " + (fSize - 8) + "(sp)");
    out.emit("addi sp, sp, " + fSize);
    out.emit("jr   ra");
  }
}
