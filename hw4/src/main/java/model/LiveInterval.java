package model;

public class LiveInterval {
  public String var;
  public int start;
  public int end;

  public LiveInterval(String var, int start, int end) {
    this.var = var;
    this.start = start;
    this.end = end;
  }

  @Override
  public String toString() {
    return "[" + var + " : " + start + "-" + end + "]";
  }
}
