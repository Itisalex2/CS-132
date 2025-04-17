package type;

import java.util.Objects;

/**
 * Represents a user-defined class name used as a type in MiniJava.
 * For example, in "class A { B b; }", B is a ClassType.
 */
public class ClassType implements MJType {
  private final String name;

  public ClassType(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  @Override
  public boolean equals(Object o) {
    return o instanceof ClassType && ((ClassType) o).name.equals(this.name);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name);
  }

  @Override
  public String toString() {
    return "ClassType(" + name + ")";
  }
}
