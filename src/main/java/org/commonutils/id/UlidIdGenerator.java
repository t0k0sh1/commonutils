package org.commonutils.id;

import java.time.Clock;
import java.time.Instant;
import java.util.Objects;
import java.util.random.RandomGenerator;
import java.util.random.RandomGeneratorFactory;
import org.commonutils.annotation.NonNull;
import org.commonutils.annotation.Nullable;

/**
 * {@link IdGenerator} for <a href="https://github.com/ulid/spec">ULID</a> strings (Crockford Base32
 * encoding of 48-bit Unix epoch milliseconds and 80 bits of randomness). Constructors choose either
 * a fixed {@link Instant} (each {@link #generate()} reuses its truncated millisecond) or a {@link
 * Clock} (each {@link #generate()} uses {@link Clock#instant()} at call time). Static {@link
 * #nonCryptographic()} and {@link #nonCryptographic(Clock)} use a fast {@link
 * java.util.SplittableRandom}-backed {@link RandomGenerator}; see the API note below.
 *
 * <p>The no-argument forms that omit a {@link RandomGenerator} use the same cryptographically
 * strong source as {@link Ulid#fromInstant(Instant)}. When a generator is supplied, thread safety
 * matches that of the given generator; if it is not thread-safe, callers must synchronize
 * externally or confine use to one thread.
 *
 * <p><strong>API note:</strong> {@link #nonCryptographic()} and {@link #nonCryptographic(Clock)}
 * prioritize speed over cryptographic unpredictability and are <strong>unsuitable</strong> for
 * secrets, session identifiers in hostile environments, or other security-sensitive identifiers
 * unless you deliberately accept weaker entropy.
 *
 * <p>Public constructors validate references with {@link NullPointerException} when {@code null} is
 * not permitted.
 *
 * @since 0.2.0
 */
public final class UlidIdGenerator implements IdGenerator<String> {

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
  public UlidIdGenerator(final @NonNull Instant instant) {
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
   * @param randomGenerator source of random payload bytes, must not be {@code null}
   * @throws NullPointerException if any argument is {@code null}
   * @throws IllegalArgumentException if the instant is not representable as a 48-bit unsigned Unix
   *     timestamp (see {@link UuidV7})
   * @since 0.2.0
   */
  public UlidIdGenerator(
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
  public UlidIdGenerator(final @NonNull Clock clock) {
    this.clock = Objects.requireNonNull(clock, "clock");
    fixedInstant = null;
    randomGenerator = null;
  }

  /**
   * Uses {@link Clock#instant()} on each {@link #generate()} call with the supplied {@link
   * RandomGenerator}.
   *
   * @param clock clock to read, must not be {@code null}
   * @param randomGenerator source of random payload bytes, must not be {@code null}
   * @throws NullPointerException if any argument is {@code null}
   * @since 0.2.0
   */
  public UlidIdGenerator(
      final @NonNull Clock clock, final @NonNull RandomGenerator randomGenerator) {
    this.clock = Objects.requireNonNull(clock, "clock");
    Objects.requireNonNull(randomGenerator, "randomGenerator");
    fixedInstant = null;
    this.randomGenerator = randomGenerator;
  }

  /**
   * Returns a generator that uses {@link Clock#systemUTC()} and a new {@link
   * java.util.SplittableRandom}-backed {@link RandomGenerator}. See class documentation for
   * security guidance.
   *
   * @return a non-null generator
   * @since 0.2.0
   */
  public static @NonNull UlidIdGenerator nonCryptographic() {
    return new UlidIdGenerator(Clock.systemUTC(), splittableRandomGenerator());
  }

  /**
   * Returns a generator that uses the given {@code clock} and a new {@link
   * java.util.SplittableRandom}-backed {@link RandomGenerator}. See class documentation for
   * security guidance.
   *
   * @param clock clock to read, must not be {@code null}
   * @return a non-null generator
   * @throws NullPointerException if {@code clock} is {@code null}
   * @since 0.2.0
   */
  public static @NonNull UlidIdGenerator nonCryptographic(final @NonNull Clock clock) {
    Objects.requireNonNull(clock, "clock");
    return new UlidIdGenerator(clock, splittableRandomGenerator());
  }

  private static @NonNull RandomGenerator splittableRandomGenerator() {
    return RandomGeneratorFactory.of("SplittableRandom").create();
  }

  /**
   * Generates a new ULID string.
   *
   * @return a non-null canonical ULID string of length {@link Ulid#ENCODED_LENGTH}
   * @throws IllegalArgumentException if this generator uses a {@link Clock} and the current instant
   *     is not representable as a 48-bit unsigned Unix timestamp
   * @since 0.2.0
   */
  @Override
  public @NonNull String generate() {
    final RandomGenerator local = randomGenerator;
    if (fixedInstant != null) {
      if (local == null) {
        return Ulid.fromInstant(fixedInstant);
      }
      return Ulid.fromInstant(fixedInstant, local);
    }
    if (local == null) {
      return Ulid.fromClock(clock);
    }
    return Ulid.fromClock(clock, local);
  }
}
