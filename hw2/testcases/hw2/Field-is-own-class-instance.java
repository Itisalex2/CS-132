class Main {
  public static void main(String[] a) {
    System.out.println(5);
  }
}

class A {
  A a;

  public int method() {
    a = new A();
    return 5;
  }
}
