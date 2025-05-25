package visitor;

import java.util.ArrayList;
import java.util.Arrays;
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
import sparrowv.Move_Reg_Reg;
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

  List<Register> callerSavedRegisters = new ArrayList<>(
      Arrays.asList(new Register("t2"), new Register("t3"), new Register("t4"), new Register("t5")));
  List<Register> calleeSavedRegisters = new ArrayList<>(
      Arrays.asList(new Register("s1"), new Register("s2"), new Register("s3"), new Register("s4"),
          new Register("s5"), new Register("s6"), new Register("s7"), new Register("s8"), new Register("s9"),
          new Register("s10"), new Register("s11")));
  List<Register> argRegisters = new ArrayList<>(
      Arrays.asList(new Register("a2"), new Register("a3"), new Register("a4"), new Register("a5"), new Register("a6"),
          new Register("a7")));

  int stackCounter = 0; // Counter for generating unique stack identifiers

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
    // for (Register calleeSavedRegister : calleeSavedRegisters) {
    // Identifier calleeSavedId = genStackIdentifier(calleeSavedRegister);
    // translatedInstructions.add(new Move_Id_Reg(calleeSavedId,
    // calleeSavedRegister));
    // }

    for (Identifier p : n.parent.formalParameters) {
      String r = allocator.getRegister(functionName, p.toString());
      if (r != null && !allocator.isSpilled(functionName, p.toString())) {
        translatedInstructions.add(new Move_Reg_Id(new Register(r), p));
      }
    }

    for (sparrow.Instruction instruction : instructions) {
      translatedInstructions.addAll(instruction.accept(this, functionName).getInstructions());
    }

    translatedInstructions.add(loadFrom(functionName, returnId, t0));
    translatedInstructions.add(new Move_Id_Reg(returnId, t0));

    // for (Register calleeSavedRegister : calleeSavedRegisters) {
    // Identifier calleeSavedId = genStackIdentifier(calleeSavedRegister);
    // translatedInstructions.add(new Move_Reg_Id(calleeSavedRegister,
    // calleeSavedId));
    // }
    Block translatedBlock = new Block(translatedInstructions, returnId);
    return TranslationResult.ofBlock(translatedBlock);
  }

  /* Label label; */
  public TranslationResult visit(sparrow.LabelInstr n, String funcName) {
    Label label = n.label;
    List<Instruction> instructions = new ArrayList<>();
    instructions.add(new LabelInstr(label));
    return TranslationResult.ofInstructions(instructions);
  }

  /*
   * Identifier lhs;
   * int rhs;
   */
  public TranslationResult visit(sparrow.Move_Id_Integer n, String funcName) {
    Identifier lhs = n.lhs;
    int rhs = n.rhs;

    List<Instruction> instructions = new ArrayList<>();
    instructions.add(new Move_Reg_Integer(t0, rhs));
    instructions.add(storeTo(funcName, lhs, t0));

    return TranslationResult.ofInstructions(instructions);
  }

  /*
   * Identifier lhs;
   * FunctionName rhs;
   */
  public TranslationResult visit(sparrow.Move_Id_FuncName n, String funcName) {
    Identifier lhs = n.lhs;
    FunctionName rhs = n.rhs;

    List<Instruction> instructions = new ArrayList<>();
    instructions.add(new Move_Reg_FuncName(t0, rhs));
    instructions.add(storeTo(funcName, lhs, t0));

    return TranslationResult.ofInstructions(instructions);
  }

  /*
   * Identifier lhs;
   * Identifier arg1;
   * Identifier arg2;
   */
  public TranslationResult visit(sparrow.Add n, String funcName) {
    Identifier lhs = n.lhs;
    Identifier arg1 = n.arg1;
    Identifier arg2 = n.arg2;

    List<Instruction> instructions = new ArrayList<>();
    instructions.add(loadFrom(funcName, arg1, t0));
    instructions.add(loadFrom(funcName, arg2, t1));
    instructions.add(new Add(t0, t0, t1));
    instructions.add(storeTo(funcName, lhs, t0));

    return TranslationResult.ofInstructions(instructions);
  }

  /*
   * Identifier lhs;
   * Identifier arg1;
   * Identifier arg2;
   */
  public TranslationResult visit(sparrow.Subtract n, String funcName) {
    Identifier lhs = n.lhs;
    Identifier arg1 = n.arg1;
    Identifier arg2 = n.arg2;

    List<Instruction> instructions = new ArrayList<>();
    instructions.add(loadFrom(funcName, arg1, t0));
    instructions.add(loadFrom(funcName, arg2, t1));
    instructions.add(new Subtract(t0, t0, t1));
    instructions.add(storeTo(funcName, lhs, t0));

    return TranslationResult.ofInstructions(instructions);
  }

  /*
   * Identifier lhs;
   * Identifier arg1;
   * Identifier arg2;
   */
  public TranslationResult visit(sparrow.Multiply n, String funcName) {
    Identifier lhs = n.lhs;
    Identifier arg1 = n.arg1;
    Identifier arg2 = n.arg2;

    List<Instruction> instructions = new ArrayList<>();
    instructions.add(loadFrom(funcName, arg1, t0));
    instructions.add(loadFrom(funcName, arg2, t1));
    instructions.add(new Multiply(t0, t0, t1));
    instructions.add(storeTo(funcName, lhs, t0));

    return TranslationResult.ofInstructions(instructions);
  }

  /*
   * Identifier lhs;
   * Identifier arg1;
   * Identifier arg2;
   */
  public TranslationResult visit(sparrow.LessThan n, String funcName) {
    Identifier lhs = n.lhs;
    Identifier arg1 = n.arg1;
    Identifier arg2 = n.arg2;

    List<Instruction> instructions = new ArrayList<>();
    instructions.add(loadFrom(funcName, arg1, t0));
    instructions.add(loadFrom(funcName, arg2, t1));
    instructions.add(new LessThan(t0, t0, t1));
    instructions.add(storeTo(funcName, lhs, t0));

    return TranslationResult.ofInstructions(instructions);
  }

  /*
   * Identifier lhs;
   * Identifier base;
   * int offset;
   */
  public TranslationResult visit(sparrow.Load n, String funcName) {
    Identifier lhs = n.lhs;
    Identifier base = n.base;
    int offset = n.offset;

    List<Instruction> instructions = new ArrayList<>();
    instructions.add(loadFrom(funcName, base, t0));
    instructions.add(new Load(t0, t0, offset));
    instructions.add(storeTo(funcName, lhs, t0));

    return TranslationResult.ofInstructions(instructions);
  }

  /*
   * Identifier base;
   * int offset;
   * Identifier rhs;
   */
  public TranslationResult visit(sparrow.Store n, String funcName) {
    Identifier base = n.base;
    int offset = n.offset;
    Identifier rhs = n.rhs;

    List<Instruction> instructions = new ArrayList<>();
    instructions.add(loadFrom(funcName, base, t0));
    instructions.add(loadFrom(funcName, rhs, t1));
    instructions.add(new Store(t0, offset, t1));

    return TranslationResult.ofInstructions(instructions);
  }

  /*
   * Identifier lhs;
   * Identifier rhs;
   */
  public TranslationResult visit(sparrow.Move_Id_Id n, String funcName) {
    Identifier lhs = n.lhs;
    Identifier rhs = n.rhs;

    List<Instruction> instructions = new ArrayList<>();
    instructions.add(loadFrom(funcName, rhs, t0));
    instructions.add(storeTo(funcName, lhs, t0));

    return TranslationResult.ofInstructions(instructions);
  }

  /*
   * Identifier lhs;
   * Identifier size;
   */
  public TranslationResult visit(sparrow.Alloc n, String funcName) {
    Identifier lhs = n.lhs;
    Identifier size = n.size;

    List<Instruction> instructions = new ArrayList<>();
    instructions.add(loadFrom(funcName, size, t0));
    instructions.add(new Alloc(t0, t0));
    instructions.add(storeTo(funcName, lhs, t0));

    return TranslationResult.ofInstructions(instructions);
  }

  /* Identifier content; */
  public TranslationResult visit(sparrow.Print n, String funcName) {
    Identifier content = n.content;

    List<Instruction> instructions = new ArrayList<>();
    instructions.add(loadFrom(funcName, content, t0));
    instructions.add(new Print(t0));

    return TranslationResult.ofInstructions(instructions);
  }

  /* String msg; */
  public TranslationResult visit(sparrow.ErrorMessage n, String funcName) {
    String msg = n.msg;

    List<Instruction> instructions = new ArrayList<>();
    instructions.add(new ErrorMessage(msg));

    return TranslationResult.ofInstructions(instructions);
  }

  /* Label label; */
  public TranslationResult visit(sparrow.Goto n, String funcName) {
    Label label = n.label;

    List<Instruction> instructions = new ArrayList<>();
    instructions.add(new Goto(label));

    return TranslationResult.ofInstructions(instructions);
  }

  /*
   * Identifier condition;
   * Label label;
   */
  public TranslationResult visit(sparrow.IfGoto n, String funcName) {
    Identifier condition = n.condition;
    Label label = n.label;

    List<Instruction> instructions = new ArrayList<>();
    instructions.add(loadFrom(funcName, condition, t0));
    instructions.add(new IfGoto(t0, label));

    return TranslationResult.ofInstructions(instructions);
  }

  /*
   * Identifier lhs;
   * Identifier callee;
   * List<Identifier> args;
   */
  public TranslationResult visit(sparrow.Call n, String funcName) {
    Identifier lhs = n.lhs;
    Identifier callee = n.callee;
    List<Identifier> args = n.args;

    List<Instruction> instructions = new ArrayList<>();
    // for (Register callerSavedRegister : callerSavedRegisters) {
    // Identifier callerSavedId = genStackIdentifier(callerSavedRegister);
    // instructions.add(new Move_Id_Reg(callerSavedId, callerSavedRegister));
    // }

    List<Identifier> saved = new ArrayList<>();
    for (Register r : callerSavedRegisters) {
      Identifier slot = new Identifier("stack_" + r + "_" + (++stackCounter));
      instructions.add(new Move_Id_Reg(slot, r)); // save once
      saved.add(slot); // remember that EXACT slot
    }

    // for (Register argRegister : argRegisters) {
    // Identifier argId = genStackIdentifier(argRegister);
    // instructions.add(new Move_Id_Reg(argId, argRegister));
    // }

    instructions.add(loadFrom(funcName, callee, t0));
    for (Identifier arg : args) {
      instructions.add(loadFrom(funcName, arg, t1));
      instructions.add(new Move_Id_Reg(arg, t1));
    }
    instructions.add(new Call(t0, t0, args));

    // for (Register callerSavedRegister : callerSavedRegisters) {
    // Identifier callerSavedId = genStackIdentifier(callerSavedRegister);
    // instructions.add(new Move_Reg_Id(callerSavedRegister, callerSavedId));
    // }

    for (int i = 0; i < callerSavedRegisters.size(); i++) {
      instructions.add(new Move_Reg_Id(callerSavedRegisters.get(i),
          saved.get(i))); // restore
    }

    // for (Register argRegister : argRegisters) {
    // Identifier argId = genStackIdentifier(argRegister);
    // instructions.add(new Move_Reg_Id(argRegister, argId));
    // }

    instructions.add(storeTo(funcName, lhs, t0));

    return TranslationResult.ofInstructions(instructions);
  }

  private Instruction storeTo(String funcName, Identifier destId, Register reg) {
    if (allocator.isSpilled(funcName, destId.toString())
        || allocator.getRegister(funcName, destId.toString()) == null) {
      return new Move_Id_Reg(destId, reg);
    } else {
      Register destReg = new Register(allocator.getRegister(funcName, destId.toString()));
      return new Move_Reg_Reg(destReg, reg);
    }
  }

  private Instruction loadFrom(String funcName, Identifier srcId, Register reg) {
    if (allocator.isSpilled(funcName, srcId.toString()) || allocator.getRegister(funcName, srcId.toString()) == null) {
      return new Move_Reg_Id(reg, srcId);
    } else {
      Register srcReg = new Register(allocator.getRegister(funcName, srcId.toString()));
      return new Move_Reg_Reg(reg, srcReg);
    }
  }
}
