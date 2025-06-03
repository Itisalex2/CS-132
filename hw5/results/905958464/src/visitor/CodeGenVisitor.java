package visitor;

import IR.token.Identifier;
import backend.Emitter;
import model.FunctionContext;
import model.TranslationContext;
import sparrowv.*;
import sparrowv.visitor.ArgVisitor;

public class CodeGenVisitor implements ArgVisitor<FunctionContext> {
  private final TranslationContext tc;
  private final Emitter out = new Emitter();
  private int ifCounter = 0;

  public CodeGenVisitor(TranslationContext tc) {
    this.tc = tc;
  }

  public String generate(Program p) {
    emitBoilerplate();
    emitTextSectionStart();
    p.accept(this, null);
    emitTextSectionEnd();
    emitDataSection();
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
    String funcName = n.functionName.toString();
    if (funcName.equals("main")) {
      funcName = "Main";
    }
    out.emit(".globl " + funcName);
    out.emit(funcName + ":");
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
    out.dec();
    out.emit(mangle(ctx, n.label.toString()) + ":");
    out.inc();
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
    out.emit("sw   " + n.rhs + ", " + ctx.offsetOf(n.lhs.toString()) + "(fp)");
  }

  /*
   * Register lhs;
   * Identifier rhs;
   */
  @Override
  public void visit(Move_Reg_Id n, FunctionContext ctx) {
    out.emit("lw   " + n.lhs + ", " + ctx.offsetOf(n.rhs.toString()) + "(fp)");
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
    String msg = n.msg.toString().replace("\"", "");
    System.err.println("Error message: " + msg);
    String sym;
    if (msg.equals("null pointer")) {
      sym = "msg_nullptr";
    } else if (msg.equals("array index out of bounds")) {
      sym = "msg_array_oob";
    } else {
      throw new IllegalStateException("unknown runtime error msg: " + msg);
    }
    out.emit("la   a0, " + sym);
    out.emit("jal  error");
  }

  /* Label label; */
  @Override
  public void visit(Goto n, FunctionContext ctx) {
    out.emit("jal  " + mangle(ctx, n.label.toString()));
  }

  /*
   * Register condition;
   * Label label;
   */
  @Override
  public void visit(IfGoto n, FunctionContext ctx) {
    String target = mangle(ctx, n.label.toString());
    String skip = ctx.getFunctionName()
        + "_no_long_jump" + (ifCounter++);

    out.emit("bnez " + n.condition + ", " + skip);
    out.emit("jal  " + target);
    out.dec();
    out.emit(skip + ":");
    out.inc();
  }

  /*
   * Register lhs;
   * Register callee;
   * List<Identifier> args;
   */
  @Override
  public void visit(Call n, FunctionContext ctx) {
    int k = n.args.size();
    int bytes = 4 * k;

    if (k > 0)
      out.emit("addi sp, sp, -" + bytes);

    for (int i = 0; i < k; i++) {
      Identifier arg = n.args.get(i);
      int off = ctx.offsetOf(arg.toString());

      out.emit("lw   t6, " + off + "(fp)");
      out.emit("sw   t6, " + (4 * i) + "(sp)");
    }

    out.emit("jalr " + n.callee);

    if (k > 0)
      out.emit("addi sp, sp, " + bytes);
    out.emit("mv   " + n.lhs + ", a0");
  }

  private void emitPrologue(FunctionContext ctx) {
    int fSize = ctx.getFrameSize();

    out.emit("sw   fp, -8(sp)");
    out.emit("mv   fp, sp");
    out.emit("addi sp, sp, -" + fSize);
    out.emit("sw   ra, -4(fp)");
  }

  private void emitEpilogue(FunctionContext ctx) {
    int fSize = ctx.getFrameSize();

    out.emit("lw   a0, " + ctx.offsetOf(ctx.getReturnId()) + "(fp)");
    out.emit("lw   ra, -4(fp)");
    out.emit("lw   fp, -8(fp)");
    out.emit("addi sp, sp, " + fSize);
    out.emit("jr   ra");
  }

  private String mangle(FunctionContext ctx, String raw) {
    return ctx.getFunctionName() + "_" + raw;
  }

  private void emitBoilerplate() {
    out.emit(".equiv @sbrk, 9");
    out.emit(".equiv @print_string, 4");
    out.emit(".equiv @print_char, 11");
    out.emit(".equiv @print_int, 1");
    out.emit(".equiv @exit, 10");
    out.emit(".equiv @exit2, 17");
    out.emit("");
  }

  private void emitTextSectionStart() {
    out.emit(".text");
    out.emit("");
    out.emit(".globl main"); // loader entry
    out.emit("    jal Main"); // jump to user Main
    out.emit("    li a0, @exit");
    out.emit("    ecall");
    out.emit("");
  }

  private void emitTextSectionEnd() {
    out.emit(".globl print"); // runtime helpers
    out.emit("print:");
    out.emit("    mv a1, a0");
    out.emit("    li a0, @print_int");
    out.emit("    ecall");
    out.emit("    li a1, 10");
    out.emit("    li a0, @print_char");
    out.emit("    ecall");
    out.emit("    jr ra");
    out.emit("");
    out.emit(".globl error");
    out.emit("error:");
    out.emit("    mv a1, a0");
    out.emit("    li a0, @print_string");
    out.emit("    ecall");
    out.emit("    li a1, 10");
    out.emit("    li a0, @print_char");
    out.emit("    ecall");
    out.emit("    li a0, @exit");
    out.emit("    ecall");
    out.emit("abort_17:");
    out.emit("    j abort_17");
    out.emit("");
    out.emit(".globl alloc");
    out.emit("alloc:");
    out.emit("    mv a1, a0");
    out.emit("    li a0, @sbrk");
    out.emit("    ecall");
    out.emit("    jr ra");
    out.emit("");
  }

  private void emitDataSection() {
    out.emit(".data");
    out.emit("");
    out.emit(".globl msg_nullptr");
    out.emit("msg_nullptr:");
    out.emit("    .asciiz \"null pointer\"");
    out.emit("    .align 2");
    out.emit("");
    out.emit(".globl msg_array_oob");
    out.emit("msg_array_oob:");
    out.emit("    .asciiz \"array index out of bounds\"");
    out.emit("    .align 2");
  }
}
