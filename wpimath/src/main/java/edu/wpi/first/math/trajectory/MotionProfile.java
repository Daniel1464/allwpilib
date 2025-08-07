// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package edu.wpi.first.math.trajectory;

/**
 * Represents a generic motion profile that computes a setpoint between a current state and a goal
 * state.
 */
@FunctionalInterface
public interface MotionProfile {
  ProfileState calculate(double t, ProfileState current, ProfileState goal);
}
