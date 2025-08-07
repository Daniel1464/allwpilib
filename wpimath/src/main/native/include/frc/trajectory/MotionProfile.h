#pragma once
#include "frc/trajectory/ProfileState.h"

namespace frc {

/**
 * Represents a generic motion profile
 */
template <class Distance>
class MotionProfile {
  using State = ProfileState<Distance>;

  virtual constexpr State Calculate(const units::second_t& t, const State& current,
                                    const State& goal) const; 
};

} // namespace frc