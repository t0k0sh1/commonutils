package org.commonutils.internal;

import org.commonutils.annotation.NonNegative;
import org.commonutils.annotation.Positive;

/**
 * Internal helpers enforcing {@link NonNegative} and {@link Positive} contracts at runtime. Not
 * exported from the {@code org.commonutils} module — for code inside this artifact only (e.g.
 * {@code org.commonutils.lang}).
 *
 * @see NonNegative
 * @see Positive
 */
public final class Contracts {
  private Contracts() {}

  public static void requireNonNegative(final String name, final int value) {
    if (value < 0) {
      throw new IllegalArgumentException(name + " (" + value + ") must be non-negative");
    }
  }

  public static void requireNonNegative(final String name, final long value) {
    if (value < 0L) {
      throw new IllegalArgumentException(name + " (" + value + ") must be non-negative");
    }
  }

  public static void requirePositive(final String name, final int value) {
    if (value <= 0) {
      throw new IllegalArgumentException(name + " (" + value + ") must be positive");
    }
  }
}
