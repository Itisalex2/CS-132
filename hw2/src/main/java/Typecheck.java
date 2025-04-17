import constant.OutputMessage;
import context.TypecheckContext;
import minijava.MiniJavaParser;
import minijava.ParseException;
import minijava.syntaxtree.Goal;
import myVisitor.SymbolTableBuilderVisitor;
import myVisitor.TypecheckVisitor;
import symbolTable.SymbolTable;

public class Typecheck {
  public static void main(String[] args) {
    try {
      Goal root = new MiniJavaParser(System.in).Goal();
      SymbolTableBuilderVisitor symbolTableBuilderVisitor = new SymbolTableBuilderVisitor();
      root.accept(symbolTableBuilderVisitor, null);

      SymbolTable symbolTable = symbolTableBuilderVisitor.getSymbolTable();
      System.err.println(symbolTable);

      TypecheckVisitor typecheckVisitor = new TypecheckVisitor();
      TypecheckContext typecheckContext = new TypecheckContext(symbolTable, null, null);
      root.accept(typecheckVisitor, typecheckContext);
    } catch (ParseException e) {
      System.out.println(e.toString());
    }

    OutputMessage.outputSuccess();
  }
}
