import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import utils.Scanner;
import utils.Token;

public class ScannerTest {
  Scanner scanner;

  @Before
  public void setUp() {
    scanner = new Scanner();
  }

  @Test
  public void testScannerBasic() {
    Scanner scanner = new Scanner();
    List<Token> tokens = scanner.scan("System.out.println(true)");

    List<Token> expectedTokens = Arrays.asList(
        Token.SYSTEM_OUT_PRINTLN,
        Token.LPAREN,
        Token.TRUE,
        Token.RPAREN,
        Token.EOF);
    assertEquals(expectedTokens, tokens);
  }

  @Test
  public void testScannerWithBraces() {
    List<Token> tokens = scanner.scan("{    System.out.println(!false);   System.out.println(true true);  }");
    List<Token> expectedTokens = Arrays.asList(
        Token.LBRACE,
        Token.SYSTEM_OUT_PRINTLN,
        Token.LPAREN,
        Token.NOT,
        Token.FALSE,
        Token.RPAREN,
        Token.SEMICOLON,
        Token.SYSTEM_OUT_PRINTLN,
        Token.LPAREN,
        Token.TRUE,
        Token.TRUE, // "true true" are 2 tokens: TRUE, TRUE
        Token.RPAREN,
        Token.SEMICOLON,
        Token.RBRACE,
        Token.EOF);
    assertEquals(expectedTokens, tokens);
  }

  @Test
  public void testScannerIfElseWhile() {
    String input = "{ if (true) System.out.println(!false);"
        + " else System.out.println(false);"
        + " while (true) { System.out.println(!!false); System.out.println(true); }";
    List<Token> tokens = scanner.scan(input);

    List<Token> expectedTokens = Arrays.asList(
        Token.LBRACE,
        Token.IF,
        Token.LPAREN,
        Token.TRUE,
        Token.RPAREN,
        Token.SYSTEM_OUT_PRINTLN,
        Token.LPAREN,
        Token.NOT,
        Token.FALSE,
        Token.RPAREN,
        Token.SEMICOLON,
        Token.ELSE,
        Token.SYSTEM_OUT_PRINTLN,
        Token.LPAREN,
        Token.FALSE,
        Token.RPAREN,
        Token.SEMICOLON,
        Token.WHILE,
        Token.LPAREN,
        Token.TRUE,
        Token.RPAREN,
        Token.LBRACE,
        Token.SYSTEM_OUT_PRINTLN,
        Token.LPAREN,
        Token.NOT,
        Token.NOT,
        Token.FALSE,
        Token.RPAREN,
        Token.SEMICOLON,
        Token.SYSTEM_OUT_PRINTLN,
        Token.LPAREN,
        Token.TRUE,
        Token.RPAREN,
        Token.SEMICOLON,
        Token.RBRACE,
        Token.EOF);
    assertEquals(expectedTokens, tokens);
  }

  @Test
  public void testEmptyInput() {
    List<Token> tokens = scanner.scan("");
    List<Token> expectedTokens = Arrays.asList(Token.EOF);
    assertEquals(expectedTokens, tokens);
  }

  @Test
  public void testSingleTokens() {
    List<Token> tokens = scanner.scan("true");
    List<Token> expected = Arrays.asList(Token.TRUE, Token.EOF);
    assertEquals(expected, tokens);

    tokens = scanner.scan("false");
    expected = Arrays.asList(Token.FALSE, Token.EOF);
    assertEquals(expected, tokens);

    tokens = scanner.scan("!");
    expected = Arrays.asList(Token.NOT, Token.EOF);
    assertEquals(expected, tokens);

    tokens = scanner.scan("System.out.println");
    expected = Arrays.asList(Token.SYSTEM_OUT_PRINTLN, Token.EOF);
    assertEquals(expected, tokens);
  }

  @Test
  public void testMixedWhitespace() {
    String input = "\n  \tSystem.out.println\t(\ntrue\t)\t{ }";
    List<Token> tokens = scanner.scan(input);

    List<Token> expectedTokens = Arrays.asList(
        Token.SYSTEM_OUT_PRINTLN,
        Token.LPAREN,
        Token.TRUE,
        Token.RPAREN,
        Token.LBRACE,
        Token.RBRACE,
        Token.EOF);
    assertEquals(expectedTokens, tokens);
  }

  @Test
  public void testMultipleExclamation() {
    String input = "!!true";
    List<Token> tokens = scanner.scan(input);

    List<Token> expectedTokens = Arrays.asList(
        Token.NOT,
        Token.NOT,
        Token.TRUE,
        Token.EOF);
    assertEquals(expectedTokens, tokens);
  }
}
