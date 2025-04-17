package type;

/*
 * Void return type is only possible for Minj Java main method
 *
 */
public class VoidType implements MJType {
  public static final VoidType INSTANCE = new VoidType();

  private VoidType() {
  }

  @Override
  public boolean equals(Object o) {
    return o instanceof VoidType;
  }

  @Override
  public int hashCode() {
    return 0;
  }

  @Override
  public String toString() {
    return "void";
  }
}
