package type;

import constant.OutputMessage;
import minijava.syntaxtree.Identifier;
import minijava.syntaxtree.NodeChoice;

public class TypeFactory {

  public static MJType getTypeFromNodeChoice(NodeChoice typeNode) {
    int which = typeNode.which;
    switch (which) {
      case 0:
        return PrimitiveType.INT_ARRAY;
      case 1:
        return PrimitiveType.BOOLEAN;
      case 2:
        return PrimitiveType.INT;
      case 3: {
        Identifier identifier = (Identifier) typeNode.choice;
        return new ClassType(identifier.f0.toString());
      }
      default:
        System.err.println("Invalid NodeChoice type: " + typeNode);
        OutputMessage.outputErrorAndExit();
        throw new AssertionError("Should not reach here");
    }
  }
}
