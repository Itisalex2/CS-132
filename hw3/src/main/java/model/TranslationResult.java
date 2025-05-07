package model;

import java.util.List;

import sparrow.Block;
import sparrow.FunctionDecl;
import sparrow.Program;

public class TranslationResult {
  private Program program;
  private Block block;
  private List<FunctionDecl> functions;
  private SparrowResult sparrowResult;

  private TranslationResult() {
  }

  public static TranslationResult ofProgram(Program program) {
    TranslationResult result = new TranslationResult();
    result.program = program;
    return result;
  }

  public static TranslationResult ofFunctions(List<FunctionDecl> functions) {
    TranslationResult result = new TranslationResult();
    result.functions = functions;
    return result;
  }

  public static TranslationResult ofBlock(Block block) {
    TranslationResult result = new TranslationResult();
    result.block = block;
    return result;
  }

  public static TranslationResult ofSparrowResult(SparrowResult sparrowResult) {
    TranslationResult result = new TranslationResult();
    result.sparrowResult = sparrowResult;
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

  public List<FunctionDecl> getFunctions() {
    if (!isFunctions()) {
      throw new IllegalStateException("Not a list of functions");
    }
    return functions;
  }

  public Block getBlock() {
    if (!isBlock()) {
      throw new IllegalStateException("Not a block");
    }
    return block;
  }

  public SparrowResult getSparrowResult() {
    if (!isSparrowResult()) {
      throw new IllegalStateException("Not a Sparrow result");
    }
    return sparrowResult;
  }

  public boolean isProgram() {
    return program != null;
  }

  public boolean isFunctions() {
    return functions != null;
  }

  public boolean isBlock() {
    return block != null;
  }

  public boolean isSparrowResult() {
    return sparrowResult != null;
  }

  @Override
  public String toString() {
    if (isProgram())
      return "TranslationResult(program)";
    if (isBlock())
      return "TranslationResult(block)";
    if (isFunctions())
      return "TranslationResult(functions)";
    if (isSparrowResult())
      return "TranslationResult(expr)";
    return "TranslationResult(empty)";
  }
}
