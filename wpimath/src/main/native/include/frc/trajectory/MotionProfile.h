

/**
 * Represents a generic motion profile
 */
template <class Distance>
class MotionProfile {
  virtual State Calculate(const units::second_t& t, const State& current,
                            const State& goal) const {}
};