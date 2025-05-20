import java.io.InputStream;

import IR.SparrowParser;
import IR.visitor.SparrowConstructor;
import model.FastLivelinessModel;
import visitor.FastLivelinessVisitor;
import IR.syntaxtree.Node;

public class S2SV {
  public static void main(String[] args) throws Exception {
    InputStream in = System.in;
    new SparrowParser(in);
    Node root = SparrowParser.Program();
    SparrowConstructor constructor = new SparrowConstructor();
    root.accept(constructor);
    sparrow.Program program = constructor.getProgram();
    System.err.println(program.toString());

    FastLivelinessVisitor fastLivelinessVisitor = new FastLivelinessVisitor();
    fastLivelinessVisitor.visit(program, null);

    FastLivelinessModel fastLivelinessModel = fastLivelinessVisitor.getFastLivelinessModel();
    System.err.println(fastLivelinessModel.toString());
  }
}
