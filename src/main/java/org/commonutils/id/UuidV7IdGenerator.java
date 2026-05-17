package org.commonutils.id;

import java.time.Clock;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import java.util.random.RandomGenerator;
import org.commonutils.annotation.NonNull;
import org.commonutils.annotation.Nullable;

/**
 * {@link IdGenerator} for <a href="https://www.rfc-editor.org/rfc/rfc9562">RFC 9562</a> UUID
 * version 7. Constructors choose either a fixed {@link Instant} (each {@link #generate()} reuses
 * its truncated millisecond) or a {@link Clock} (each {@link #generate()} uses {@link
 * Clock#instant()} at call time). Batch generation is provided by {@link UuidV7}.
 *
 * <p>The no-argument forms that omit a {@link RandomGenerator} use the same cryptographically
 * strong source as {@link UuidV7#fromInstant(Instant)}. When a generator is supplied, thread safety
 * matches that of the given generator; if it is not thread-safe, callers must synchronize
 * externally or confine use to one thread.
 *
 * <p>Public constructors validate references with {@link NullPointerException} when {@code null} is
 * not permitted.
 *
 * @since 0.2.0
 */
public final class UuidV7IdGenerator implements IdGenerator<UUID> {

  private final @Nullable Instant fixedInstant;
  private final @Nullable Clock clock;
  private final @Nullable RandomGenerator randomGenerator;

  /**
   * Uses a fixed instant and a cryptographically strong {@link RandomGenerator} on each {@link
   * #generate()} call.
   *
   * @param instant instant whose millisecond value is embedded repeatedly, must not be {@code null}
   * @throws NullPointerException if {@code instant} is {@code null}
   * @throws IllegalArgumentException if the instant is not representable as a 48-bit unsigned Unix
   *     timestamp (see {@link UuidV7})
   * @since 0.2.0
   */
  public UuidV7IdGenerator(final @NonNull Instant instant) {
    Objects.requireNonNull(instant, "instant");
    UuidV7.requireUnixMillis(instant);
    fixedInstant = instant;
    clock = null;
    randomGenerator = null;
  }

  /**
   * Uses a fixed instant and the supplied {@link RandomGenerator} on each {@link #generate()} call.
   *
   * @param instant instant whose millisecond value is embedded repeatedly, must not be {@code null}
   * @param randomGenerator source of random payload bits, must not be {@code null}
   * @throws NullPointerException if any argument is {@code null}
   * @throws IllegalArgumentException if the instant is not representable as a 48-bit unsigned Unix
   *     timestamp (see {@link UuidV7})
   * @since 0.2.0
   */
  public UuidV7IdGenerator(
      final @NonNull Instant instant, final @NonNull RandomGenerator randomGenerator) {
    Objects.requireNonNull(instant, "instant");
    Objects.requireNonNull(randomGenerator, "randomGenerator");
    UuidV7.requireUnixMillis(instant);
    fixedInstant = instant;
    clock = null;
    this.randomGenerator = randomGenerator;
  }

  /**
   * Uses {@link Clock#instant()} on each {@link #generate()} call with a cryptographically strong
   * {@link RandomGenerator}.
   *
   * @param clock clock to read, must not be {@code null}
   * @throws NullPointerException if {@code clock} is {@code null}
   * @since 0.2.0
   */
  public UuidV7IdGenerator(final @NonNull Clock clock) {
    this.clock = Objects.requireNonNull(clock, "clock");
    fixedInstant = null;
    randomGenerator = null;
  }

  /**
   * Uses {@link Clock#instant()} on each {@link #generate()} call with the supplied {@link
   * RandomGenerator}.
   *
   * @param clock clock to read, must not be {@code null}
   * @param randomGenerator source of random payload bits, must not be {@code null}
   * @throws NullPointerException if any argument is {@code null}
   * @since 0.2.0
   */
  public UuidV7IdGenerator(
      final @NonNull Clock clock, final @NonNull RandomGenerator randomGenerator) {
    this.clock = Objects.requireNonNull(clock, "clock");
    Objects.requireNonNull(randomGenerator, "randomGenerator");
    fixedInstant = null;
    this.randomGenerator = randomGenerator;
  }

  /**
   * Generates a new UUID v7.
   *
   * @return a non-null UUID per RFC 9562 version 7 and this generator's configuration
   * @throws IllegalArgumentException if this generator uses a {@link Clock} and the current instant
   *     is not representable as a 48-bit unsigned Unix timestamp
   * @since 0.2.0
   */
  @Override
  public @NonNull UUID generate() {
    final RandomGenerator local = randomGenerator;
    if (fixedInstant != null) {
      if (local == null) {
        return UuidV7.fromInstant(fixedInstant);
      }
      return UuidV7.fromInstant(fixedInstant, local);
    }
    if (local == null) {
      return UuidV7.fromClock(clock);
    }
    return UuidV7.fromClock(clock, local);
  }
}
