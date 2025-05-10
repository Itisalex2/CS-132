class Main {
  public static void main(String[] args) {
    C c;
  }
}

class D {
  public int compute(int val) {
    return 0;
  }
}

class C extends D {
  public int compute(int val) {
    int i;
    int[] sharedArray;
    sharedArray = new int[val];
    i = 0;
    while (i < (sharedArray.length)) {
      i = i + 1;
    }
    return 0;
  }
}
