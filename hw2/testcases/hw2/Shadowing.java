class Main {
  public static void main(String[] a) {
    System.out.println(5);
  }
}

class A {
  boolean x;
}

class B extends A {
  int y;

  public int foo(int x) {
    int z;
    int result;

    z = 5;
    result = x + y;
    result = result + z;

    return result;
  }
}
