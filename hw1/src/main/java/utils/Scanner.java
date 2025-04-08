package utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Scanner {
  private final Map<StateCharPair, Integer> stateTransitionTable = createStateTransitionTable();
  private final Map<Integer, Token> stateAcceptanceTable = createStateAcceptanceTable();

  public List<Token> scan(String input) {
    List<Token> tokens = new ArrayList<>();
    int state = 0;

    for (char c : input.toCharArray()) {
      StateCharPair pair = new StateCharPair(state, c);

      if (state == 0 && (c == ' ' || c == '\n' || c == '\t')) {
        continue;
      }

      if (stateTransitionTable.containsKey(pair)) {
        state = stateTransitionTable.get(pair);

        if (stateAcceptanceTable.containsKey(state)) {
          tokens.add(stateAcceptanceTable.get(state));
          state = 0;
        }

      } else {
        throw new IllegalArgumentException("Invalid character: " + c);
      }
    }

    if (state != 0) {
      throw new IllegalArgumentException("Undetermined token");
    }

    tokens.add(Token.EOF);

    return tokens;
  }

  class StateCharPair {
    private final int state;
    private final char c;

    public StateCharPair(int state, char c) {
      this.state = state;
      this.c = c;
    }

    public int getState() {
      return state;
    }

    public char getChar() {
      return c;
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj)
        return true;
      if (obj == null || getClass() != obj.getClass())
        return false;
      StateCharPair other = (StateCharPair) obj;
      return state == other.state && c == other.c;
    }

    @Override
    public int hashCode() {
      return 31 * state + c;
    }
  }

  private Map<StateCharPair, Integer> createStateTransitionTable() {
    Map<StateCharPair, Integer> stateTransitionTable = new HashMap<>();

    // System.out.println
    stateTransitionTable.put(new StateCharPair(0, 'S'), 1);
    stateTransitionTable.put(new StateCharPair(1, 'y'), 2);
    stateTransitionTable.put(new StateCharPair(2, 's'), 3);
    stateTransitionTable.put(new StateCharPair(3, 't'), 4);
    stateTransitionTable.put(new StateCharPair(4, 'e'), 5);
    stateTransitionTable.put(new StateCharPair(5, 'm'), 6);
    stateTransitionTable.put(new StateCharPair(6, '.'), 7);
    stateTransitionTable.put(new StateCharPair(7, 'o'), 8);
    stateTransitionTable.put(new StateCharPair(8, 'u'), 9);
    stateTransitionTable.put(new StateCharPair(9, 't'), 10);
    stateTransitionTable.put(new StateCharPair(10, '.'), 11);
    stateTransitionTable.put(new StateCharPair(11, 'p'), 12);
    stateTransitionTable.put(new StateCharPair(12, 'r'), 13);
    stateTransitionTable.put(new StateCharPair(13, 'i'), 14);
    stateTransitionTable.put(new StateCharPair(14, 'n'), 15);
    stateTransitionTable.put(new StateCharPair(15, 't'), 16);
    stateTransitionTable.put(new StateCharPair(16, 'l'), 17);
    stateTransitionTable.put(new StateCharPair(17, 'n'), 18);

    // {}
    stateTransitionTable.put(new StateCharPair(0, '{'), 19);
    stateTransitionTable.put(new StateCharPair(0, '}'), 20);

    // ()
    stateTransitionTable.put(new StateCharPair(0, '('), 21);
    stateTransitionTable.put(new StateCharPair(0, ')'), 22);

    // !;
    stateTransitionTable.put(new StateCharPair(0, '!'), 23);
    stateTransitionTable.put(new StateCharPair(0, ';'), 24);

    // if
    stateTransitionTable.put(new StateCharPair(0, 'i'), 25);
    stateTransitionTable.put(new StateCharPair(25, 'f'), 26);

    // else
    stateTransitionTable.put(new StateCharPair(0, 'e'), 27);
    stateTransitionTable.put(new StateCharPair(27, 'l'), 28);
    stateTransitionTable.put(new StateCharPair(28, 's'), 29);
    stateTransitionTable.put(new StateCharPair(29, 'e'), 30);

    // while
    stateTransitionTable.put(new StateCharPair(0, 'w'), 31);
    stateTransitionTable.put(new StateCharPair(31, 'h'), 32);
    stateTransitionTable.put(new StateCharPair(32, 'i'), 33);
    stateTransitionTable.put(new StateCharPair(33, 'l'), 34);
    stateTransitionTable.put(new StateCharPair(34, 'e'), 35);

    // true
    stateTransitionTable.put(new StateCharPair(0, 't'), 36);
    stateTransitionTable.put(new StateCharPair(36, 'r'), 37);
    stateTransitionTable.put(new StateCharPair(37, 'u'), 38);
    stateTransitionTable.put(new StateCharPair(38, 'e'), 39);

    // false
    stateTransitionTable.put(new StateCharPair(0, 'f'), 40);
    stateTransitionTable.put(new StateCharPair(40, 'a'), 41);
    stateTransitionTable.put(new StateCharPair(41, 'l'), 42);
    stateTransitionTable.put(new StateCharPair(42, 's'), 43);
    stateTransitionTable.put(new StateCharPair(43, 'e'), 44);

    return stateTransitionTable;
  }

  public Map<Integer, Token> createStateAcceptanceTable() {
    Map<Integer, Token> stateAcceptanceTable = new HashMap<>();

    // System.out.println
    stateAcceptanceTable.put(18, Token.SYSTEM_OUT_PRINTLN);

    // {}
    stateAcceptanceTable.put(19, Token.LBRACE);
    stateAcceptanceTable.put(20, Token.RBRACE);

    // ()
    stateAcceptanceTable.put(21, Token.LPAREN);
    stateAcceptanceTable.put(22, Token.RPAREN);

    // !;
    stateAcceptanceTable.put(23, Token.NOT);
    stateAcceptanceTable.put(24, Token.SEMICOLON);

    // if
    stateAcceptanceTable.put(26, Token.IF);

    // else
    stateAcceptanceTable.put(30, Token.ELSE);

    // while
    stateAcceptanceTable.put(35, Token.WHILE);

    // true
    stateAcceptanceTable.put(39, Token.TRUE);

    // false
    stateAcceptanceTable.put(44, Token.FALSE);

    return stateAcceptanceTable;
  }
}
