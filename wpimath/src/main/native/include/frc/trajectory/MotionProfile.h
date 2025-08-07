#include "frc/trajectory/ProfileState.h"

/**
 * Represents a generic motion profile
 */
template <class Distance>
class MotionProfile {
  using State = ProfileState<Distance>;

  virtual State Calculate(const units::second_t& t, const State& current,
                            const State& goal) const {}
};