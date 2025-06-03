package visitor;

import java.util.Iterator;
import java.util.LinkedHashSet;

import model.TranslationContext;
import sparrowv.Block;
import sparrowv.FunctionDecl;
import sparrowv.Instruction;
import sparrowv.Move_Id_Reg;
import sparrowv.visitor.DepthFirst;

public class TranslationContextVisitor extends DepthFirst {
  String currentFunctionName;
  TranslationContext translationContext = new TranslationContext();

  public TranslationContext getTranslationContext() {
    return translationContext;
  }

  @Override
  public void visit(FunctionDecl n) {
    currentFunctionName = n.functionName.toString();
    translationContext.addFunctionContext(currentFunctionName,
        new model.FunctionContext(n.formalParameters.size(), new LinkedHashSet<>()));

    n.block.accept(this);
  }

  @Override
  public void visit(Block n) {
    Iterator<Instruction> var2 = n.instructions.iterator();

    while (var2.hasNext()) {
      Instruction i = (Instruction) var2.next();
      i.accept(this);
    }
  }

  @Override
  public void visit(Move_Id_Reg n) {
    translationContext.getFunctionContext(currentFunctionName)
        .addLocalVariable(n.lhs.toString());
  }

  @Override
  public String toString() {
    return "TranslationContextVisitor{" +
        "currentFunctionName='" + currentFunctionName + '\'' +
        ", translationContext=" + translationContext +
        '}';
  }
}
