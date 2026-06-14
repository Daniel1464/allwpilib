// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package org.wpilib.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Enforces a compile-time constraint on a subset of double/int parameters on a constructor or
 * method.
 *
 * <p>This annotation will not throw an error when the value of a function parameter cannot be
 * determined at compile-time.
 *
 * <pre>{@code
 * class Feedforward {
 *     @NumericConstraint(expect = {"kV >= 0", "kA >= 0"}, error = "length is 6")
 *     public Feedforward(double kS, double kV, double kA) {}
 * }
 *
 * new Feedforward(0, 1, 1); // OK - constraint is satisfied
 * new Feedforward(0, -1, 1); // Compile-time error: kV < 0
 * }</pre>
 */
@Target({ElementType.CONSTRUCTOR, ElementType.METHOD})
@Retention(RetentionPolicy.CLASS)
public @interface NumericConstraint {
  /**
   * Specifies the expected constraints as a set of String expressions. These constraints are
   * checked against the annotated method or constructor parameters at compile-time. The constraints
   * must be valid expressions defining rules or conditions for the parameters.
   *
   * @return An array of constraint expressions that must be satisfied.
   */
  String[] expect();

  /**
   * A custom error message to display if the constraints are not satisfied.
   *
   * @return A custom error message.
   */
  String error() default "";
}
