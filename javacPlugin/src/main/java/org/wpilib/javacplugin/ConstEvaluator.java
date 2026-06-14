// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package org.wpilib.javacplugin;

import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.LiteralTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.UnaryTree;
import com.sun.source.util.TreePath;
import com.sun.source.util.Trees;
import javax.lang.model.element.VariableElement;

/** A utility class for evaluating expressions that can be evaluated at compile-time. */
public final class ConstEvaluator {
  /**
   * Evaluates a compile-time evaluatable numeric expression.
   *
   * @param tree the Trees instance
   * @param root the CompilationUnitTree
   * @param expr the expression to evaluate
   * @return the evaluated expression, or null if the expression cannot be evaluated
   */
  public static Number evaluateNumber(Trees tree, CompilationUnitTree root, ExpressionTree expr) {
    return switch (expr) {
      case null -> null;

      // Literal like 0, 1, etc.
      case LiteralTree lit when lit.getValue() instanceof Number num -> num;

      // Handle unary minus of an int literal, e.g., -1
      case UnaryTree unary
          when unary.getKind() == Tree.Kind.UNARY_MINUS
              && unary.getExpression() instanceof LiteralTree literal
              && literal.getValue() instanceof Number num ->
          num.doubleValue() * -1;

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
