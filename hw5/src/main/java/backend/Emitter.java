package backend;

public final class Emitter {
  private final StringBuilder sb = new StringBuilder();
  private int indent = 0;
  private static final String IND = "    ";

  public void inc() {
    indent++;
  }

  public void dec() {
    indent--;
  }

  public void emit(String s) {
    for (int i = 0; i < indent; i++)
      sb.append(IND);
    sb.append(s).append('\n');
  }

  @Override
  public String toString() {
    return sb.toString();
  }
}
