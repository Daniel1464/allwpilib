package org.wpilib.javacplugin;

import com.sun.source.tree.*;
import com.sun.source.util.TreePath;
import com.sun.source.util.Trees;

import javax.lang.model.element.VariableElement;

public final class ConstEvaluator {
  public static Number evaluateNumber(Trees tree, CompilationUnitTree root, ExpressionTree expr) {
    return switch (expr) {
      case null -> null;

      // Literal like 0, 1, etc.
      case LiteralTree lit when lit.getValue() instanceof Number num -> num;

      // Handle unary minus of an int literal, e.g., -1
      case UnaryTree unary
        when unary.getKind() == Tree.Kind.UNARY_MINUS
        && unary.getExpression() instanceof LiteralTree literal
        && literal.getValue() instanceof Number num -> num.doubleValue() * -1;

      default -> {
        // Handle references to compile-time constants (static final int)
        TreePath path = tree.getPath(root, expr);
        if (path != null
            && tree.getElement(path) instanceof VariableElement var
            && var.getConstantValue() instanceof Number num) {
          yield num;
        }
        yield null;
      }
    };
  }

  private ConstEvaluator() {}
}
