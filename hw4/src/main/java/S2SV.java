import java.io.InputStream;

import IR.SparrowParser;
import IR.visitor.SparrowConstructor;
import algorithm.LinearScanRegisterAllocator;
import model.FastLivelinessModel;
import visitor.FastLivelinessVisitor;
import visitor.TranslationVisitor;
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

    LinearScanRegisterAllocator linearScanRegisterAllocator = new LinearScanRegisterAllocator(fastLivelinessModel, 4);
    linearScanRegisterAllocator.computeRegisterAllocationTable();
    System.err.println(linearScanRegisterAllocator);

    TranslationVisitor translationVisitor = new TranslationVisitor(linearScanRegisterAllocator);
    translationVisitor.visit(program, null);
    System.out.println(translationVisitor.getProgram().toString());
  }
}
