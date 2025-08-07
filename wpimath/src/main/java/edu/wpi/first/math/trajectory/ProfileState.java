// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package edu.wpi.first.math.trajectory;

import java.util.Objects;

/** Represents a "state" of a {@link TrapezoidProfile} or {@link ExponentialProfile}. */
public class ProfileState {
  /** The position at this state. */
  public double position;

  /** The velocity at this state. */
  public double velocity;

  /** Default constructor. */
  public ProfileState() {}

  /**
   * Constructs a state for a trapezoidal or exponential motion profile.
   *
   * @param position The position at this state.
   * @param velocity The velocity at this state.
   */
  public ProfileState(double position, double velocity) {
    this.position = position;
    this.velocity = velocity;
  }

  @Override
  public boolean equals(Object other) {
    return other instanceof ProfileState rhs
        && this.position == rhs.position
        && this.velocity == rhs.velocity;
  }

  @Override
  public int hashCode() {
    return Objects.hash(position, velocity);
  }
}
