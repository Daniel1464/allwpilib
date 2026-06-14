// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package org.wpilib.javacplugin;

import com.sun.source.tree.*;
import com.sun.source.util.*;
import org.wpilib.annotation.NumericConstraint;

import javax.lang.model.element.ExecutableElement;
import javax.tools.Diagnostic;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;

public class NumericConstraintDetector implements TaskListener {
  public record Operator(
    String repr,
    Function<String, Boolean> hasOperator,
    BiFunction<Double, Double, Boolean> comparator
  ) {
    Operator(String repr, BiFunction<Double, Double, Boolean> comparator) {
      this(repr, s -> s.contains(repr), comparator);
    }
  }

  public static final Operator[] OPERATORS = {
    new Operator(">", expr -> expr.contains(">") && !expr.contains(">="), (a, b) -> a > b),
    new Operator("<", expr -> expr.contains("<") && !expr.contains("<="), (a, b) -> a < b),
    new Operator(">=", (a, b) -> a >= b),
    new Operator("<=", (a, b) -> a <= b),
    new Operator("!=", (a, b) -> Math.abs(a - b) > 1e-9),
  };

  private final JavacTask m_task;
  private final Set<CompilationUnitTree> m_visitedCUs = new HashSet<>();

  public NumericConstraintDetector(JavacTask task) {
    m_task = task;
  }

  @Override
  public void finished(TaskEvent e) {
    if (e.getKind() == TaskEvent.Kind.ANALYZE && m_visitedCUs.add(e.getCompilationUnit())) {
      e.getCompilationUnit().accept(new Scanner(e.getCompilationUnit()), null);
    }
  }

  private final class Scanner extends TreeScanner<Void, Void> {
    private final CompilationUnitTree m_root;
    private final Trees m_trees;

    Scanner(CompilationUnitTree compilationUnit) {
      m_root = compilationUnit;
      m_trees = Trees.instance(m_task);
    }

    @Override
    public Void visitMethodInvocation(MethodInvocationTree node, Void unused) {
      checkNumericConstraint(node, node.getArguments());
      return super.visitMethodInvocation(node, unused);
    }

    @Override
    public Void visitNewClass(NewClassTree node, Void unused) {
      checkNumericConstraint(node, node.getArguments());
      return super.visitNewClass(node, unused);
    }

    private void checkNumericConstraint(Tree node, List<? extends ExpressionTree> args) {
      var path = m_trees.getPath(m_root, node);
      if (path == null) {
        return;
      }
      var el = m_trees.getElement(path);
      if (!(el instanceof ExecutableElement method)) {
        return;
      }
      var anno = method.getAnnotation(NumericConstraint.class);
      if (anno == null) {
        return;
      }
      var params = method.getParameters();
      var variables = new HashMap<String, Double>();
      int limit = Math.min(params.size(), args.size());

      for (int i = 0; i < limit; i++) {
        var paramName = params.get(i).getSimpleName().toString();
        var constVal = ConstEvaluator.evaluateNumber(m_trees, m_root, args.get(i));
        if (constVal != null) {
            variables.put(paramName, constVal.doubleValue());
        }
      }
      for (var constraint: anno.expect()) {
        for (var operator: OPERATORS) {
          if (!operator.hasOperator().apply(constraint)) {
            continue;
          }
          var constraintArgs = constraint.replace(" ", "").split(operator.repr());
          if (constraintArgs.length != 2) {
            continue;
          }
          var value1 = parseConstraintVar(variables, constraintArgs[0]);
          var value2 = parseConstraintVar(variables, constraintArgs[1]);
          if (value1 == null || value2 == null || operator.comparator.apply(value1, value2)) {
            continue;
          }
          var msg = anno.error().isEmpty()
            ? ("Constructor/method call did not satisfy constraint: " + constraint)
            : anno.error();
          m_trees.printMessage(Diagnostic.Kind.ERROR, msg, node, m_root);
        }
      }
    }

    private Double parseConstraintVar(Map<String, Double> variableDecls, String arg) {
      try {
        return Double.parseDouble(arg);
      } catch (NumberFormatException ex) {
        return variableDecls.get(arg);
      }
    }
  }
}
