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

      if (symbolTable.containsCycle()) {
        System.err.println("Symbol table contains a cycle.");
        OutputMessage.outputErrorAndExit();
      }

      if (symbolTable.containsOverload()) {
        System.err.println("Symbol table contains an overload.");
        OutputMessage.outputErrorAndExit();
      }

      TypecheckVisitor typecheckVisitor = new TypecheckVisitor();
      TypecheckContext typecheckContext = new TypecheckContext(symbolTable, null, null);
      root.accept(typecheckVisitor, typecheckContext);
    } catch (ParseException e) {
      System.out.println(e.toString());
    }

    OutputMessage.outputSuccess();
  }
}
