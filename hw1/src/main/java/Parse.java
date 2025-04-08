import java.util.List;

import utils.InputReader;
import utils.Scanner;
import utils.Token;

public class Parse {
  private Token currentToken;
  private int tokenIndex = -1;
  private List<Token> tokens;

  public static void main(String[] args) {
    Parse parse = new Parse();
    parse.parse();
  }

  private void parse() {
    Scanner scanner = new Scanner();
    tokens = null;

    try {
      String input = InputReader.readAll();
      tokens = scanner.scan(input);
    } catch (Exception e) {
      printParseError();
      return;
    }

    currentToken = nextToken();
    S();

    if (currentToken == Token.EOF) {
      printParseSucess();
    } else {
      printParseError();
    }
  }

  private Token nextToken() {
    tokenIndex++;

    if (tokenIndex >= tokens.size()) {
      return Token.EOF;
    }

    Token token = tokens.get(tokenIndex);
    return token;
  }

  private void eat(Token token) {
    if (currentToken.equals(token)) {
      currentToken = nextToken();
    } else {
      printParseError();
      System.exit(1);
    }
  }

  private void S() {
    if (currentToken == Token.LBRACE) {
      eat(Token.LBRACE);
      L();
      eat(Token.RBRACE);
    } else if (currentToken == Token.SYSTEM_OUT_PRINTLN) {
      eat(Token.SYSTEM_OUT_PRINTLN);
      eat(Token.LPAREN);
      E();
      eat(Token.RPAREN);
      eat(Token.SEMICOLON);
    } else if (currentToken == Token.IF) {
      eat(Token.IF);
      eat(Token.LPAREN);
      E();
      eat(Token.RPAREN);
      S();
      eat(Token.ELSE);
      S();
    } else if (currentToken == Token.WHILE) {
      eat(Token.WHILE);
      eat(Token.LPAREN);
      E();
      eat(Token.RPAREN);
      S();
    } else {
      printParseError();
      System.exit(1);
    }
  }

  private void L() {
    if (currentToken == Token.LBRACE ||
        currentToken == Token.SYSTEM_OUT_PRINTLN ||
        currentToken == Token.IF ||
        currentToken == Token.WHILE) {
      S();
      L();
    }
  }

  private void E() {
    if (currentToken == Token.TRUE) {
      eat(Token.TRUE);
    } else if (currentToken == Token.FALSE) {
      eat(Token.FALSE);
    } else if (currentToken == Token.NOT) {
      eat(Token.NOT);
      E();
    } else {
      printParseError();
      System.exit(1);
    }
  }

  private static void printParseError() {
    System.out.println("Parse error");
  }

  private static void printParseSucess() {
    System.out.println("Program parsed successfully");
  }
}
