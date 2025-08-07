#include "units/math.h"
#include "units/time.h"

/**
 * Profile state.
 */
template <class Distance, class Input>
class State {
  public:
    using Velocity =
      units::compound_unit<Distance, units::inverse<units::seconds>>;
    /// The position at this state.
    units::unit_t<Distance> position{0};

    /// The velocity at this state.
    units::unit_t<Velocity> velocity{0};

  constexpr bool operator==(const State&) const = default;
};