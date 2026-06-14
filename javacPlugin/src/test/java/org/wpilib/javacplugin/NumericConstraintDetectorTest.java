// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package org.wpilib.javacplugin;

import static com.google.testing.compile.CompilationSubject.assertThat;
import static com.google.testing.compile.Compiler.javac;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.wpilib.javacplugin.CompileTestUtils.kJavaVersionOptions;

import com.google.testing.compile.Compilation;
import com.google.testing.compile.JavaFileObjects;
import org.junit.jupiter.api.Test;

class NumericConstraintDetectorTest {
  @Test
  void testConstructorConstraintSuccess() {
    String source =
        """
        package frc.robot;

        import org.wpilib.annotation.NumericConstraint;

        class MyMotor {
          @NumericConstraint(expect = {"id >= 0", "canBusId >= 0"}, error = "Invalid ID/CANBus ID")
          public MyMotor(int id, int canBusId) {}

          void test() {
            new MyMotor(1, 2);
          }
        }
        """;

    Compilation compilation =
        javac()
            .withOptions(kJavaVersionOptions)
            .compile(JavaFileObjects.forSourceString("frc.robot.MyMotor", source));

    assertThat(compilation).succeededWithoutWarnings();
  }

  @Test
  void testConstructorConstraintViolation() {
    String source =
        """
        package frc.robot;

        import org.wpilib.annotation.NumericConstraint;

        class MyMotor {
          @NumericConstraint(expect = {"id >= 0", "canBusId >= 0"}, error = "Invalid ID/CANBus ID")
          public MyMotor(int id, int canBusId) {}

          void test() {
            new MyMotor(-1, 2);
          }
        }
        """;

    Compilation compilation =
        javac()
            .withOptions(kJavaVersionOptions)
            .compile(JavaFileObjects.forSourceString("frc.robot.MyMotor", source));

    assertThat(compilation).failed();
    assertEquals(1, compilation.errors().size());
    assertEquals("Invalid ID/CANBus ID", compilation.errors().get(0).getMessage(null));
  }

  @Test
  void testDefaultErrorMessage() {
    String source =
        """
        package frc.robot;

        import org.wpilib.annotation.NumericConstraint;

        class MyMotor {
          @NumericConstraint(expect = "id >= 0")
          public MyMotor(int id) {}

          void test() {
            new MyMotor(-5);
          }
        }
        """;

    Compilation compilation =
        javac()
            .withOptions(kJavaVersionOptions)
            .compile(JavaFileObjects.forSourceString("frc.robot.MyMotor", source));

    assertThat(compilation).failed();
    assertEquals(1, compilation.errors().size());
    assertEquals(
        "Constructor/method call did not satisfy constraint: id >= 0",
        compilation.errors().get(0).getMessage(null));
  }

  @Test
  void testMethodConstraintViolation() {
    String source =
        """
        package frc.robot;

        import org.wpilib.annotation.NumericConstraint;

        class TestClass {
          @NumericConstraint(
            expect = {"ratio > 0", "ratio < 1.0"},
            error = "Ratio must be between 0 and 1"
          )
          void setRatio(double ratio) {}

          void test() {
            setRatio(0);
          }
        }
        """;

    Compilation compilation =
        javac()
            .withOptions(kJavaVersionOptions)
            .compile(JavaFileObjects.forSourceString("frc.robot.TestClass", source));

    assertThat(compilation).failed();
    assertEquals(1, compilation.errors().size());
    assertEquals("Ratio must be between 0 and 1", compilation.errors().get(0).getMessage(null));
  }

  @Test
  void testNonConstantArgumentsSkipped() {
    String source =
        """
        package frc.robot;

        import org.wpilib.annotation.NumericConstraint;

        class MyMotor {
          @NumericConstraint(expect = {"id >= 0", "canBusId >= 0"}, error = "Invalid ID/CANBus ID")
          public MyMotor(int id, int canBusId) {}

          void test(int dynamicId) {
            new MyMotor(dynamicId, 2);
          }
        }
        """;

    Compilation compilation =
        javac()
            .withOptions(kJavaVersionOptions)
            .compile(JavaFileObjects.forSourceString("frc.robot.MyMotor", source));

    assertThat(compilation).succeededWithoutWarnings();
  }

  @Test
  void testDeclarationNonExistentParameter() {
    String source =
        """
        package frc.robot;

        import org.wpilib.annotation.NumericConstraint;

        class MyMotor {
          @NumericConstraint(expect = "typoId >= 0", error = "Invalid parameter name")
          public MyMotor(int id) {}
        }
        """;

    Compilation compilation =
        javac()
            .withOptions(kJavaVersionOptions)
            .withProcessors(new NumericConstraintProcessor())
            .compile(JavaFileObjects.forSourceString("frc.robot.MyMotor", source));

    assertThat(compilation).failed();
    assertEquals(1, compilation.errors().size());
    assertEquals(
        "Constraint has invalid reference (check your spelling): typoId >= 0",
        compilation.errors().get(0).getMessage(null));
  }
}
