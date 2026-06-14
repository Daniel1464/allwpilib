// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package org.wpilib.javacplugin;

import static org.wpilib.javacplugin.NumericConstraintDetector.OPERATORS;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.tools.Diagnostic;
import org.wpilib.annotation.NumericConstraint;

public class NumericConstraintProcessor extends AbstractProcessor {
  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
    for (Element element : roundEnv.getElementsAnnotatedWith(NumericConstraint.class)) {
      if (!(element instanceof ExecutableElement executable)) {
        printError(element, "@NumericConstraint can only be applied to methods or constructors");
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
      printError(
          element,
          "Constraints only support simple comparison operators (>, <, >=, ==, etc.)."
              + " Compound operators are not supported: "
              + constraint);
      return;
    }
    long numOperators =
        Arrays.stream(OPERATORS).filter(op -> op.hasOperator().apply(constraint)).count();
    if (numOperators > 1) {
      printError(element, "Constraint contains multiple comparison operators: " + constraint);
    } else if (numOperators == 0) {
      printError(element, "Constraint does not contain any comparison operator: " + constraint);
    }
  }

  private void validateVariableNames(Element element, String constraint, Set<String> validParams) {
    for (var op : OPERATORS) {
      if (!op.hasOperator().apply(constraint)) {
        continue;
      }
      var arguments = constraint.split(op.repr());
      if (arguments.length != 2) {
        continue;
      }
      if (validParams.contains(arguments[0].trim()) || validParams.contains(arguments[1].trim())) {
        return;
      }
      printError(element, "Constraint has invalid reference (check your spelling): " + constraint);
    }
    printError(element, "Could not parse variable from constraint: " + constraint);
  }

  private void printError(Element element, String message) {
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
