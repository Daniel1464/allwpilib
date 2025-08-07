#pragma once
#include "units/math.h"
#include "units/time.h"

namespace frc {

/**
 * Profile state.
 */
template <class Distance>
class ProfileState {
  public:
    using Velocity =
      units::compound_unit<Distance, units::inverse<units::seconds>>;
    
    /// The position at this state.
    units::unit_t<Distance> position{0};

    /// The velocity at this state.
    units::unit_t<Velocity> velocity{0};

  constexpr bool operator==(const ProfileState&) const = default;
};

} // namespace frc