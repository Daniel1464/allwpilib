package org.wpilib.javacplugin;

import org.wpilib.annotation.NumericConstraint;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.*;
import javax.lang.model.SourceVersion;
import javax.tools.Diagnostic;
import java.util.*;

import static org.wpilib.javacplugin.NumericConstraintDetector.OPERATORS;

public class NumericConstraintProcessor extends AbstractProcessor {
  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
    for (Element element : roundEnv.getElementsAnnotatedWith(NumericConstraint.class)) {
      if (!(element instanceof ExecutableElement executable)) {
        error(element, "@NumericConstraint can only be applied to methods or constructors");
        continue;
      }
      var constraints = element.getAnnotation(NumericConstraint.class).expect();
      var paramNames = new HashSet<String>();
      for (VariableElement param : executable.getParameters()) {
        paramNames.add(param.getSimpleName().toString());
      }
      for (String constraint : constraints) {
        validateConstraintSyntax(element, constraint);
        validateVariableNames(element, constraint, paramNames);
      }
    }
    return true;
  }

  private void validateConstraintSyntax(Element element, String constraint) {
    if (constraint.contains("&&") || constraint.contains("||") || constraint.contains("!")) {
      error(element, "Constraints only support simple comparison operators (>, <, >=, ==, etc.). Compound operators are not supported: " + constraint);
      return;
    }
    long numOperators = Arrays.stream(OPERATORS).filter(op -> op.hasOperator().apply(constraint)).count();
    if (numOperators > 1) {
      error(element, "Constraint contains multiple comparison operators: " + constraint);
    } else if (numOperators == 0) {
      error(element, "Constraint does not contain any comparison operator: " + constraint);
    }
  }

  private void validateVariableNames(Element element, String constraint, Set<String> validParams) {
    // Extract identifier on the left side of the operator
    for (var op: OPERATORS) {
      if (!op.hasOperator().apply(constraint)) {
        continue;
      }
      var arguments = constraint.split(op.repr());
      if (arguments.length != 2) {
        continue;
      }
      if (!validParams.contains(arguments[0].trim()) && !validParams.contains(arguments[1].trim())) {
        error(element, "Constraint has invalid reference (check your spelling): " + constraint);
      }
      return;
    }
    error(element, "Could not parse variable from constraint: " + constraint);
  }

  private void error(Element element, String message) {
    processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, message, element);
  }

  @Override
  public Set<String> getSupportedAnnotationTypes() {
    return Set.of(NumericConstraint.class.getCanonicalName());
  }

  @Override
  public SourceVersion getSupportedSourceVersion() {
    return SourceVersion.latestSupported();
  }
}