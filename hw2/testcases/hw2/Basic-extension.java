class A {
  public static void main(String[] a) {
    System.out.println(new B().init());
  }
}

class B {
  int z;

  public int init() {
    z = 5;
    return z;
  }

  public int method(int x) {
    int b;
    b = x + z;
    return b;
  }
}

class C extends B {
  int y;

  public int init() {
    y = 10;
    return y;
  }

  public int method(int x) {
    int c;
    c = x + y;
    return c;
  }
}

class D extends C {
  int x;

  public int init() {
    x = 15;
    return x;
  }

  public int method(int x) {
    int d;
    d = x + x;
    return d;
  }
}
