#pragma once
#include "frc/trajectory/ProfileState.h"

namespace frc {

/**
 * Represents a generic motion profile
 */
template <class Distance>
class MotionProfile {
  public:
    virtual constexpr State Calculate(const units::second_t& t, 
                                      const ProfileState<Distance>& current,
                                      const ProfileState<Distance>& goal) const; 
};

} // namespace frc