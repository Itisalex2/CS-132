package myVisitor;

import context.TypecheckContext;
import minijava.syntaxtree.Goal;
import minijava.visitor.GJDepthFirst;
import type.MJType;

public class TypecheckVisitor extends GJDepthFirst<MJType, TypecheckContext> {
  @Override
  public MJType visit(Goal x, TypecheckContext typecheckContext) {
    return null;
  }
}
