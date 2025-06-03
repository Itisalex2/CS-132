import java.io.InputStream;

import IR.SparrowParser;
import IR.visitor.SparrowVConstructor;
import model.TranslationContext;
import IR.syntaxtree.Node;
import IR.registers.Registers;

import sparrowv.Program;
import visitor.CodeGenVisitor;
import visitor.TranslationContextVisitor;

public class SV2V {
  public static void main(String[] args) throws Exception {
    Registers.SetRiscVregs();
    InputStream in = System.in;
    new SparrowParser(in);
    Node root = SparrowParser.Program();
    SparrowVConstructor constructor = new SparrowVConstructor();
    root.accept(constructor);
    Program program = constructor.getProgram();
    TranslationContextVisitor translationContextVisitor = new TranslationContextVisitor();
    translationContextVisitor.visit(program);
    TranslationContext translationContext = translationContextVisitor.getTranslationContext();
    System.err.println("Translation Context:");
    System.err.println(translationContext);
    CodeGenVisitor codeGenVisitor = new CodeGenVisitor(translationContext);
    String translatedProgram = codeGenVisitor.generate(program);
    System.out.println(translatedProgram);
  }
}
