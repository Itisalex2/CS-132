class A {
  public static void main(String[] a) {
    System.out.println(5);
  }
}

class B {
  public int method(int x) {
    return x;
  }
}

class C extends B {
  public int method(int y) {
    return y;
  }
}
