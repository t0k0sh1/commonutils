package org.commonutils.lang;

import org.commonutils.annotation.NonNegative;
import org.commonutils.annotation.Nullable;

/**
 * Strategy for measuring the &quot;length&quot; of a {@link CharSequence}. Like {@link
 * java.util.Comparator}, callers may pass built-in implementations ({@link Lengths}) or a custom
 * lambda or class.
 *
 * <p>Implementations must return a non-negative count and treat {@code null} like an empty sequence
 * (count {@code 0}).
 */
@FunctionalInterface
public interface LengthCounter {

  /**
   * Returns a non-negative length count. A {@code null} sequence counts as {@code 0}.
   *
   * @param sequence text to measure, may be {@code null}
   * @return count per this strategy, never negative
   */
  @NonNegative
  int count(@Nullable CharSequence sequence);
}
