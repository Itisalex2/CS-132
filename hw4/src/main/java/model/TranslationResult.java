package model;

import java.util.List;

import sparrowv.Block;
import sparrowv.FunctionDecl;
import sparrowv.Instruction;
import sparrowv.Program;

public class TranslationResult {
  private Program program;
  private Block block;
  private FunctionDecl function;
  private List<Instruction> instructions;

  private TranslationResult() {
  }

  public static TranslationResult ofProgram(Program program) {
    TranslationResult result = new TranslationResult();
    result.program = program;
    return result;
  }

  public static TranslationResult ofFunctionDecl(FunctionDecl function) {
    TranslationResult result = new TranslationResult();
    result.function = function;
    return result;
  }

  public static TranslationResult ofBlock(Block block) {
    TranslationResult result = new TranslationResult();
    result.block = block;
    return result;
  }

  public static TranslationResult ofInstructions(List<Instruction> instructions) {
    TranslationResult result = new TranslationResult();
    result.instructions = instructions;
    return result;
  }

  /*
   * Fail fast for getters - might not be the best design, but it is practical
   */

  public Program getProgram() {
    if (!isProgram()) {
      throw new IllegalStateException("Not a program");
    }
    return program;
  }

  public FunctionDecl getFunctionDecl() {
    if (!isFunctionDecl()) {
      throw new IllegalStateException("Not a list of functions");
    }
    return function;
  }

  public Block getBlock() {
    if (!isBlock()) {
      throw new IllegalStateException("Not a block");
    }
    return block;
  }

  public List<Instruction> getInstructions() {
    if (!isInstructions()) {
      throw new IllegalStateException("Not instructions");
    }
    return instructions;
  }

  public boolean isProgram() {
    return program != null;
  }

  public boolean isFunctionDecl() {
    return function != null;
  }

  public boolean isBlock() {
    return block != null;
  }

  public boolean isInstructions() {
    return instructions != null;
  }

  @Override
  public String toString() {
    if (isProgram())
      return "TranslationResult(program)";
    if (isBlock())
      return "TranslationResult(block)";
    if (isFunctionDecl())
      return "TranslationResult(functionDecl)";
    if (isInstructions())
      return "TranslationResult(instructions)";
    return "TranslationResult(empty)";
  }
}
