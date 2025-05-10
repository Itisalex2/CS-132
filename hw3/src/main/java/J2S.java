import context.TranslationContext;
import minijava.MiniJavaParser;
import minijava.ParseException;
import minijava.syntaxtree.Goal;
import myVisitor.SymbolTableBuilderVisitor;
import myVisitor.TranslationVisitor;
import sparrow.Program;
import symbolTable.SymbolTable;

public class J2S {
  public static void main(String[] args) {
    try {
      Goal root = new MiniJavaParser(System.in).Goal();
      SymbolTableBuilderVisitor symbolTableBuilderVisitor = new SymbolTableBuilderVisitor();
      root.accept(symbolTableBuilderVisitor, null);

      SymbolTable symbolTable = symbolTableBuilderVisitor.getSymbolTable();
      symbolTable.resolveInheritance();
      System.err.println(symbolTable);

      TranslationVisitor translationVisitor = new TranslationVisitor();
      TranslationContext translationContext = new TranslationContext(symbolTable, null, null);
      root.accept(translationVisitor, translationContext);

      Program prog = translationVisitor.getProgram();

      System.out.println(prog.toString());
    } catch (ParseException e) {
      System.err.println("Parse error: " + e.getMessage());
      return;
    }
  }
}
