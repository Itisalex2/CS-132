package type;

public class IdentifierType implements MJType {
  public static final IdentifierType INSTANCE = new IdentifierType();

  private IdentifierType() {
  }

  @Override
  public boolean equals(Object o) {
    return o instanceof IdentifierType;
  }

  @Override
  public int hashCode() {
    return 1;
  }

  @Override
  public String toString() {
    return "identifier";
  }
}
