package model;

import IR.token.Identifier;
import constant.OutputMessage;
import sparrow.Instruction;
import type.MJType;

import java.util.List;

/**
 * Represents the result of translating a MiniJava expression
 * into Sparrow instructions.
 */
public class SparrowResult {
  private final List<Instruction> instructions;
  private final Identifier result;
  private MJType type = null;

  public SparrowResult(List<Instruction> instructions, Identifier result) {
    this.instructions = instructions;
    this.result = result;
  }

  public SparrowResult withType(MJType type) {
    this.type = type;
    return this;
  }

  public List<Instruction> getInstructions() {
    return instructions;
  }

  public Identifier getResult() {
    return result;
  }

  public MJType getType() {
    if (type == null) {
      throw new IllegalStateException("Type has not been set for " + result);
    }
    return type;
  }
}
