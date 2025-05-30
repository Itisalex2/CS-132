package model;

public class LiveInterval {
  public String var;
  public int start;
  public int end;
  public boolean containsCall;

  public LiveInterval(String var, int start, int end, boolean containsCall) {
    this.var = var;
    this.start = start;
    this.end = end;
    this.containsCall = containsCall;
  }

  @Override
  public String toString() {
    return "[" + var + " : " + start + "-" + end + "]";
  }
}
