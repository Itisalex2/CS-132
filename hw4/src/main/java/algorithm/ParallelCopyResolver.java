package algorithm;

import java.util.*;

import IR.token.Identifier;
import IR.token.Register;
import sparrowv.Instruction;
import sparrowv.Move_Reg_Id;
import sparrowv.Move_Reg_Reg;

/** Utility for resolving a parallel–copy (register shuffle) before a call. */
public final class ParallelCopyResolver {

  public abstract static class Loc {
  }

  public static final class RegLoc extends Loc {
    public final Register r;

    public RegLoc(Register r) {
      this.r = r;
    }
  }

  public static final class MemLoc extends Loc {
    public final Identifier slot;

    public MemLoc(Identifier slot) {
      this.slot = slot;
    }
  }

  public static final class Move {
    public final RegLoc dst; // always a register on the left
    public final Loc src; // register or memory on the right

    public Move(RegLoc dst, Loc src) {
      this.dst = dst;
      this.src = src;
    }
  }

  public static List<Instruction> resolveMoves(List<Move> moves,
      Register scratch // t0
  ) {

    List<Instruction> newInstructions = new ArrayList<>();
    Map<String, String> graph = new LinkedHashMap<>(); // dst → src
    List<Move> lateLoads = new ArrayList<>();

    /* 1. classify ------------------------------------------------ */
    for (Move m : moves) {
      if ((m.src instanceof MemLoc)) { // mem → reg
        lateLoads.add(m); // do later
      } else { // reg → reg
        graph.put(m.dst.r.toString(), ((RegLoc) m.src).r.toString());
      }
    }

    /* debug print */
    System.err.println("Parallel-copy graph:");
    for (Map.Entry<String, String> e : graph.entrySet()) {
      System.err.println("  " + e.getKey() + " <- " + e.getValue());
    }

    /* 2. peel acyclic reg→reg edges ---------------------------- */
    boolean progress;
    do {
      progress = false;
      Iterator<Map.Entry<String, String>> it = graph.entrySet().iterator();
      while (it.hasNext()) {
        Map.Entry<String, String> e = it.next();
        String dst = e.getKey();
        String src = e.getValue();
        if (!graph.containsValue(dst)) { // safe to emit
          if (!dst.equals(src)) {
            newInstructions.add(new Move_Reg_Reg(new Register(dst), new Register(src)));
          }
          it.remove();
          progress = true;
        }
      }
    } while (progress);

    System.err.println("After peeling acyclic edges:");
    System.err.println("Executed moves:");
    for (Instruction m : newInstructions) {
      System.err.println("  " + m);
    }
    System.err.println("Remaining graph:");
    for (Map.Entry<String, String> e : graph.entrySet()) {
      System.err.println("  " + e.getKey() + " <- " + e.getValue());
    }

    /* 3. break each remaining cycle with t0 -------------------- */
    List<Instruction> cycleInstructions = new ArrayList<>();
    while (!graph.isEmpty()) {
      String start = graph.keySet().iterator().next();

      cycleInstructions.add(
          new Move_Reg_Reg(scratch, new Register(start)));

      String v = start;
      while (true) {
        String next = graph.get(v);
        if (next.equals(start)) {
          cycleInstructions.add(
              new Move_Reg_Reg(new Register(v), scratch));
          graph.remove(v);
          break;
        } else {
          cycleInstructions.add(
              new Move_Reg_Reg(new Register(v), new Register(next)));
          graph.remove(v);
          v = next;
        }
      }
    }
    System.err.println("Cycle instructions:");
    for (Instruction m : cycleInstructions) {
      System.err.println("  " + m);
    }
    newInstructions.addAll(cycleInstructions);

    /* 4. perform the mem → reg loads --------------------------- */
    for (Move m : lateLoads) {
      newInstructions.add(new Move_Reg_Id(m.dst.r, ((MemLoc) m.src).slot));
    }

    return newInstructions;
  }

  private ParallelCopyResolver() {
  }
}
