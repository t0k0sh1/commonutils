package org.commonutils.id;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.random.RandomGenerator;
import java.util.random.RandomGeneratorFactory;
import org.commonutils.annotation.NonNull;
import org.commonutils.annotation.Positive;
import org.commonutils.internal.Contracts;

/**
 * Static factory for <a href="https://www.rfc-editor.org/rfc/rfc9562">RFC 9562</a> UUID version 7
 * (Unix millisecond timestamp in the most significant bits, random in the remainder).
 *
 * <p>Timestamps use {@link Instant#toEpochMilli()} semantics: values are truncated toward zero to
 * whole milliseconds (sub-millisecond components of {@link Instant} do not affect the embedded
 * {@code unix_ts_ms}). {@link Instant}s whose millisecond value is negative (before the Unix epoch)
 * or greater than {@value #MAX_UNIX_TS_MS} are rejected with {@link IllegalArgumentException}.
 *
 * <p>For {@link #batchFromInstantOrderedInRange}, when the closed millisecond range {@code [min,
 * max]} is too narrow to fit {@code count} strictly increasing timestamps ({@code max - min < count
 * - 1}), this implementation assigns timestamps with a non-decreasing staircase {@code
 * min&nbsp;+&nbsp;floor(i&nbsp;*&nbsp;(max&nbsp;-&nbsp;min)&nbsp;/&nbsp;(count&nbsp;-&nbsp;1))} for
 * {@code i} in {@code [0, count)}, filling random payload bits anew each time.
 *
 * <p>Public methods validate non-null references with {@link NullPointerException} where {@code
 * null} is not permitted, and numeric contracts with {@link IllegalArgumentException} as
 * documented.
 *
 * @since 0.2.0
 */
public final class UuidV7 {

  /** Inclusive maximum {@code unix_ts_ms} (48-bit unsigned). */
  public static final long MAX_UNIX_TS_MS = (1L << 48) - 1L;

  private static final int VERSION_7 = 7;

  private static final RandomGenerator SECURE_RANDOM =
      RandomGeneratorFactory.of("SecureRandom").create();

  private UuidV7() {}

  /**
   * Creates a UUID v7 from an instant, using a cryptographically strong {@link RandomGenerator}.
   *
   * @param instant source instant (truncated to milliseconds), must not be {@code null}
   * @return a non-null UUID v7
   * @throws NullPointerException if {@code instant} is {@code null}
   * @throws IllegalArgumentException if the instant's millisecond value is not representable as a
   *     48-bit unsigned Unix timestamp
   * @since 0.2.0
   */
  public static @NonNull UUID fromInstant(final @NonNull Instant instant) {
    return fromInstant(instant, SECURE_RANDOM);
  }

  /**
   * Creates a UUID v7 from an instant and the given random generator.
   *
   * @param instant source instant (truncated to milliseconds), must not be {@code null}
   * @param random source for {@code rand_a} and {@code rand_b}, must not be {@code null}
   * @return a non-null UUID v7
   * @throws NullPointerException if any argument is {@code null}
   * @throws IllegalArgumentException if the instant's millisecond value is not representable as a
   *     48-bit unsigned Unix timestamp
   * @since 0.2.0
   */
  public static @NonNull UUID fromInstant(
      final @NonNull Instant instant, final @NonNull RandomGenerator random) {
    Objects.requireNonNull(instant, "instant");
    Objects.requireNonNull(random, "random");
    return uuidV7(requireUnixMillis(instant), random);
  }

  /**
   * Creates a UUID v7 from {@link Clock#instant()}, using a cryptographically strong {@link
   * RandomGenerator}.
   *
   * @param clock clock to read, must not be {@code null}
   * @return a non-null UUID v7
   * @throws NullPointerException if {@code clock} is {@code null}
   * @throws IllegalArgumentException if the clock instant's millisecond value is not representable
   *     as a 48-bit unsigned Unix timestamp
   * @since 0.2.0
   */
  public static @NonNull UUID fromClock(final @NonNull Clock clock) {
    return fromClock(clock, SECURE_RANDOM);
  }

  /**
   * Creates a UUID v7 from {@link Clock#instant()} and the given random generator.
   *
   * @param clock clock to read, must not be {@code null}
   * @param random source for {@code rand_a} and {@code rand_b}, must not be {@code null}
   * @return a non-null UUID v7
   * @throws NullPointerException if any argument is {@code null}
   * @throws IllegalArgumentException if the clock instant's millisecond value is not representable
   *     as a 48-bit unsigned Unix timestamp
   * @since 0.2.0
   */
  public static @NonNull UUID fromClock(
      final @NonNull Clock clock, final @NonNull RandomGenerator random) {
    Objects.requireNonNull(clock, "clock");
    return fromInstant(clock.instant(), random);
  }

  /**
   * Returns {@code count} UUID v7 values sharing the same embedded Unix millisecond as {@code
   * instant}; random payload bits differ per element.
   *
   * @param instant shared timestamp (truncated to milliseconds), must not be {@code null}
   * @param count number of UUIDs, must be positive
   * @param random source for random payload bits, must not be {@code null}
   * @return an unmodifiable list of length {@code count}
   * @throws NullPointerException if any argument is {@code null}
   * @throws IllegalArgumentException if {@code count} is not positive or the instant is not
   *     representable as a 48-bit unsigned Unix timestamp
   * @since 0.2.0
   */
  public static @NonNull List<UUID> batchFromInstant(
      final @NonNull Instant instant,
      final @Positive int count,
      final @NonNull RandomGenerator random) {
    Objects.requireNonNull(instant, "instant");
    Objects.requireNonNull(random, "random");
    Contracts.requirePositive("count", count);
    final long unixMs = requireUnixMillis(instant);
    final ArrayList<UUID> out = new ArrayList<>(count);
    for (int i = 0; i < count; i++) {
      out.add(uuidV7(unixMs, random));
    }
    return Collections.unmodifiableList(out);
  }

  /**
   * Returns {@code count} UUID v7 values by calling {@link Clock#instant()} before each generation.
   * Embedded milliseconds are not necessarily non-decreasing if the clock moves backward.
   *
   * @param clock clock to read, must not be {@code null}
   * @param count number of UUIDs, must be positive
   * @param random source for random payload bits, must not be {@code null}
   * @return an unmodifiable list of length {@code count}
   * @throws NullPointerException if any argument is {@code null}
   * @throws IllegalArgumentException if {@code count} is not positive or any sampled instant is not
   *     representable as a 48-bit unsigned Unix timestamp
   * @since 0.2.0
   */
  public static @NonNull List<UUID> batchFromClock(
      final @NonNull Clock clock,
      final @Positive int count,
      final @NonNull RandomGenerator random) {
    Objects.requireNonNull(clock, "clock");
    Objects.requireNonNull(random, "random");
    Contracts.requirePositive("count", count);
    final ArrayList<UUID> out = new ArrayList<>(count);
    for (int i = 0; i < count; i++) {
      out.add(fromInstant(clock.instant(), random));
    }
    return Collections.unmodifiableList(out);
  }

  /**
   * Picks a uniformly random Unix millisecond in the closed range {@code [minInclusive,
   * maxInclusive]} (compared via {@link Instant#toEpochMilli()} after validation), then creates a
   * UUID v7 at that millisecond.
   *
   * @param minInclusive range lower bound (truncated to milliseconds), must not be {@code null}
   * @param maxInclusive range upper bound (truncated to milliseconds), must not be {@code null}
   * @param random source for the millisecond draw and payload bits, must not be {@code null}
   * @return a non-null UUID v7
   * @throws NullPointerException if any argument is {@code null}
   * @throws IllegalArgumentException if {@code minInclusive.compareTo(maxInclusive) > 0}, or either
   *     bound's millisecond value is not representable as a 48-bit unsigned Unix timestamp
   * @since 0.2.0
   */
  public static @NonNull UUID fromInstantRandomInRange(
      final @NonNull Instant minInclusive,
      final @NonNull Instant maxInclusive,
      final @NonNull RandomGenerator random) {
    Objects.requireNonNull(minInclusive, "minInclusive");
    Objects.requireNonNull(maxInclusive, "maxInclusive");
    Objects.requireNonNull(random, "random");
    final long minMs = requireUnixMillis(minInclusive);
    final long maxMs = requireUnixMillis(maxInclusive);
    if (minMs > maxMs) {
      throw new IllegalArgumentException("minInclusive must not be after maxInclusive");
    }
    final long unixMs = random.nextLong(minMs, maxMs + 1L);
    return uuidV7(unixMs, random);
  }

  /**
   * Returns {@code count} UUID v7 values whose embedded Unix milliseconds form a non-decreasing
   * sequence; each millisecond lies in the closed range {@code [minInclusive, maxInclusive]}
   * ({@link Instant#toEpochMilli()} after validation). When {@code maxInclusive - minInclusive >=
   * count - 1}, a random starting millisecond is chosen so that {@code start, start+1, …} stays
   * inside the range; otherwise the staircase rule described in the class documentation applies.
   *
   * @param minInclusive range lower bound (truncated to milliseconds), must not be {@code null}
   * @param maxInclusive range upper bound (truncated to milliseconds), must not be {@code null}
   * @param count number of UUIDs, must be positive
   * @param random source for random payload bits (and wide-range start selection), must not be
   *     {@code null}
   * @return an unmodifiable list of length {@code count}
   * @throws NullPointerException if any argument is {@code null}
   * @throws IllegalArgumentException if {@code minInclusive.compareTo(maxInclusive) > 0}, {@code
   *     count} is not positive, or either bound is not representable as a 48-bit unsigned Unix
   *     timestamp
   * @since 0.2.0
   */
  public static @NonNull List<UUID> batchFromInstantOrderedInRange(
      final @NonNull Instant minInclusive,
      final @NonNull Instant maxInclusive,
      final @Positive int count,
      final @NonNull RandomGenerator random) {
    Objects.requireNonNull(minInclusive, "minInclusive");
    Objects.requireNonNull(maxInclusive, "maxInclusive");
    Objects.requireNonNull(random, "random");
    Contracts.requirePositive("count", count);
    final long minMs = requireUnixMillis(minInclusive);
    final long maxMs = requireUnixMillis(maxInclusive);
    if (minMs > maxMs) {
      throw new IllegalArgumentException("minInclusive must not be after maxInclusive");
    }
    final long span = maxMs - minMs;
    final ArrayList<UUID> out = new ArrayList<>(count);
    if (count == 1) {
      final long unixMs = random.nextLong(minMs, maxMs + 1L);
      out.add(uuidV7(unixMs, random));
      return Collections.unmodifiableList(out);
    }
    if (span >= count - 1L) {
      final long startExclusiveUpper = maxMs - count + 2L;
      final long start = random.nextLong(minMs, startExclusiveUpper);
      for (int i = 0; i < count; i++) {
        out.add(uuidV7(start + i, random));
      }
    } else {
      for (int i = 0; i < count; i++) {
        final long unixMs = minMs + (i * span) / (count - 1L);
        out.add(uuidV7(unixMs, random));
      }
    }
    return Collections.unmodifiableList(out);
  }

  /**
   * Package-private encoding hook for tests (fixed timestamp and random fields).
   *
   * @param unixTsMs 48-bit Unix milliseconds
   * @param randA12 twelve low bits used for {@code rand_a}
   * @param leastSigBits least 64 bits including RFC variant ({@code 10xx …}) in the MSBs of this
   *     long
   */
  static @NonNull UUID uuidV7FromParts(
      final long unixTsMs, final int randA12, final long leastSigBits) {
    requireRepresentableUnixMillis(unixTsMs);
    final int randA = randA12 & 0xFFF;
    final long msb = msb(unixTsMs, randA);
    return new UUID(msb, leastSigBits);
  }

  static long requireUnixMillis(final @NonNull Instant instant) {
    Objects.requireNonNull(instant, "instant");
    final long ms;
    try {
      ms = instant.toEpochMilli();
    } catch (final ArithmeticException e) {
      throw new IllegalArgumentException("instant out of epoch-millis range", e);
    }
    requireRepresentableUnixMillis(ms);
    return ms;
  }

  private static void requireRepresentableUnixMillis(final long ms) {
    if (ms < 0L) {
      throw new IllegalArgumentException(
          "unix_ts_ms (" + ms + ") must be non-negative for UUID v7");
    }
    if (ms > MAX_UNIX_TS_MS) {
      throw new IllegalArgumentException(
          "unix_ts_ms (" + ms + ") exceeds 48-bit maximum (" + MAX_UNIX_TS_MS + ")");
    }
  }

  private static long msb(final long unixTsMs, final int randA12) {
    final int timeLow = (int) (unixTsMs >>> 16);
    final int timeMid = (int) (unixTsMs & 0xFFFFL);
    final int timeHiVersion = (VERSION_7 << 12) | (randA12 & 0xFFF);
    return ((long) timeLow << 32) | ((long) timeMid << 16) | (long) timeHiVersion;
  }

  private static @NonNull UUID uuidV7(final long unixTsMs, final RandomGenerator random) {
    requireRepresentableUnixMillis(unixTsMs);
    final int randA = random.nextInt(1 << 12);
    final byte[] bb = new byte[8];
    random.nextBytes(bb);
    bb[0] = (byte) ((bb[0] & 0x3F) | 0x80);
    long lsb = 0L;
    for (int i = 0; i < 8; i++) {
      lsb = (lsb << 8) | (bb[i] & 0xffL);
    }
    return new UUID(msb(unixTsMs, randA), lsb);
  }
}
