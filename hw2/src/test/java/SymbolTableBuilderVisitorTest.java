import org.junit.Test;

import myVisitor.SymbolTableBuilderVisitor;

import static org.junit.Assert.*;

import org.junit.Before;

public class SymbolTableBuilderVisitorTest {
  private SymbolTableBuilderVisitor visitor;

  @Before
  public void setUp() {
    visitor = new SymbolTableBuilderVisitor();
  }

  public void testNothing() {
    assertTrue(true);
  }
}
