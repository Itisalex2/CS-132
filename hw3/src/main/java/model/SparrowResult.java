package model;

import IR.token.Identifier;
import sparrow.Instruction;

import java.util.List;

/**
 * Represents the result of translating a MiniJava expression
 * into Sparrow instructions.
 */
public class SparrowResult {
  private final List<Instruction> instructions;
  private final Identifier result;

  public SparrowResult(List<Instruction> instructions, Identifier result) {
    this.instructions = instructions;
    this.result = result;
  }

  public List<Instruction> getInstructions() {
    return instructions;
  }

  public Identifier getResult() {
    return result;
  }
}
