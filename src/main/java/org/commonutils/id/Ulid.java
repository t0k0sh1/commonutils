package org.commonutils.id;

import java.time.Clock;
import java.time.Instant;
import java.util.Objects;
import java.util.random.RandomGenerator;
import java.util.random.RandomGeneratorFactory;
import org.commonutils.annotation.NonNull;

/**
 * Static factory for <a href="https://github.com/ulid/spec">ULID</a>s: 48-bit Unix millisecond
 * timestamp ({@link Instant#toEpochMilli()} semantics) in the most significant bits, 80 bits of
 * randomness, encoded as 26 uppercase Crockford Base32 characters.
 *
 * <p>Timestamps follow the same rules as {@link UuidV7}: {@link Instant}s whose millisecond value
 * is negative or greater than {@value UuidV7#MAX_UNIX_TS_MS} are rejected with {@link
 * IllegalArgumentException}.
 *
 * <p>The no-argument overloads of {@link #fromInstant(Instant)} and {@link #fromClock(Clock)} use
 * the same cryptographically strong {@link RandomGenerator} as {@link UuidV7}. When a generator is
 * supplied, thread safety matches that of the given generator.
 *
 * <p>Public methods validate non-null references with {@link NullPointerException} where {@code
 * null} is not permitted.
 *
 * @since 0.2.0
 */
public final class Ulid {

  /**
   * Canonical Crockford Base32 alphabet for ULID (uppercase only in generated strings); ULIDs are
   * case-insensitive when parsing elsewhere, but this library emits uppercase only.
   */
  public static final @NonNull String CROCKFORD_BASE32 = "0123456789ABCDEFGHJKMNPQRSTVWXYZ";

  /**
   * Number of characters in a canonical ULID string ({@value #TIME_CHARS} time + {@value
   * #RANDOM_CHARS} random).
   */
  public static final int ENCODED_LENGTH = 26;

  /**
   * Crockford Base32 characters encoding the 48-bit timestamp (10×5 bits, last group zero-pads by
   * two bits).
   */
  public static final int TIME_CHARS = 10;

  /** Crockford Base32 characters encoding the 80-bit random component. */
  public static final int RANDOM_CHARS = 16;

  /** Length of the random component in bytes (80 bits). */
  public static final int RANDOM_BYTES = 10;

  private static final char @NonNull [] CROCKFORD = CROCKFORD_BASE32.toCharArray();

  private static final @NonNull RandomGenerator SECURE_RANDOM =
      RandomGeneratorFactory.of("SecureRandom").create();

  private Ulid() {}

  /**
   * Creates a ULID from an instant using a cryptographically strong {@link RandomGenerator}.
   *
   * @param instant source instant (truncated to milliseconds), must not be {@code null}
   * @return a non-null canonical ULID string of length {@link #ENCODED_LENGTH}
   * @throws NullPointerException if {@code instant} is {@code null}
   * @throws IllegalArgumentException if the instant's millisecond value is not representable as a
   *     48-bit unsigned Unix timestamp
   * @since 0.2.0
   */
  public static @NonNull String fromInstant(final @NonNull Instant instant) {
    Objects.requireNonNull(instant, "instant");
    return fromInstant(instant, SECURE_RANDOM);
  }

  /**
   * Creates a ULID from {@code instant} and the given random source.
   *
   * @param instant source instant (truncated to milliseconds), must not be {@code null}
   * @param random source for {@value #RANDOM_BYTES} entropy bytes (big-endian in the encoded
   *     output), must not be {@code null}
   * @return a non-null canonical ULID string
   * @throws NullPointerException if any argument is {@code null}
   * @throws IllegalArgumentException if the instant's millisecond value is not representable as a
   *     48-bit unsigned Unix timestamp
   * @since 0.2.0
   */
  public static @NonNull String fromInstant(
      final @NonNull Instant instant, final @NonNull RandomGenerator random) {
    Objects.requireNonNull(instant, "instant");
    Objects.requireNonNull(random, "random");
    final long ms = UuidV7.requireUnixMillis(instant);
    final byte[] buf = new byte[RANDOM_BYTES];
    random.nextBytes(buf);
    return encodeValidated(ms, buf);
  }

  /**
   * Creates a ULID from {@link Clock#instant()} using a cryptographically strong {@link
   * RandomGenerator}.
   *
   * @param clock clock to read, must not be {@code null}
   * @return a non-null canonical ULID string
   * @throws NullPointerException if {@code clock} is {@code null}
   * @throws IllegalArgumentException if the clock instant's millisecond value is not representable
   *     as a 48-bit unsigned Unix timestamp
   * @since 0.2.0
   */
  public static @NonNull String fromClock(final @NonNull Clock clock) {
    Objects.requireNonNull(clock, "clock");
    return fromClock(clock, SECURE_RANDOM);
  }

  /**
   * Creates a ULID from {@link Clock#instant()} and the given random source.
   *
   * @param clock clock to read, must not be {@code null}
   * @param random source for {@value #RANDOM_BYTES} entropy bytes, must not be {@code null}
   * @return a non-null canonical ULID string
   * @throws NullPointerException if any argument is {@code null}
   * @throws IllegalArgumentException if the clock instant's millisecond value is not representable
   *     as a 48-bit unsigned Unix timestamp
   * @since 0.2.0
   */
  public static @NonNull String fromClock(
      final @NonNull Clock clock, final @NonNull RandomGenerator random) {
    Objects.requireNonNull(clock, "clock");
    Objects.requireNonNull(random, "random");
    return fromInstant(clock.instant(), random);
  }

  /**
   * Encodes a validated Unix millisecond timestamp and exactly {@value #RANDOM_BYTES} random bytes
   * into a canonical ULID string. Useful for deterministic tests; production code often prefers
   * {@link #fromInstant(Instant, RandomGenerator)}.
   *
   * @param unixTsMs Unix epoch milliseconds (same representability rules as {@link UuidV7})
   * @param randomness exactly {@value #RANDOM_BYTES} bytes interpreted as big-endian for Base32
   *     encoding of the random section, must not be {@code null}
   * @return a non-null ULID string of length {@link #ENCODED_LENGTH}
   * @throws NullPointerException if {@code randomness} is {@code null}
   * @throws IllegalArgumentException if {@code randomness.length != }{@link #RANDOM_BYTES} or
   *     {@code unixTsMs} is not representable as a 48-bit unsigned Unix timestamp
   * @since 0.2.0
   */
  public static @NonNull String encode(final long unixTsMs, final byte @NonNull [] randomness) {
    Objects.requireNonNull(randomness, "randomness");
    if (randomness.length != RANDOM_BYTES) {
      throw new IllegalArgumentException(
          "randomness must have length " + RANDOM_BYTES + ", got " + randomness.length);
    }
    final long ms = UuidV7.requireUnixMillis(Instant.ofEpochMilli(unixTsMs));
    return encodeValidated(ms, randomness);
  }

  /**
   * @param unixMs already validated by {@link UuidV7#requireUnixMillis(Instant)}
   */
  private static @NonNull String encodeValidated(
      final long unixMs, final byte @NonNull [] randomness10) {
    final char[] out = new char[ENCODED_LENGTH];
    appendTimeChars(unixMs, out, 0);
    appendRandomChars(randomness10, out, TIME_CHARS);
    return new String(out);
  }

  private static void appendTimeChars(
      final long unixMs, final char @NonNull [] out, final int offset) {
    for (int i = 0; i < 9; i++) {
      final int index = (int) ((unixMs >> (47 - i * 5)) & 0x1FL);
      out[offset + i] = CROCKFORD[index];
    }
    final int index = (int) ((unixMs & 0x7L) << 2);
    out[offset + 9] = CROCKFORD[index];
  }

  /** Interprets {@code b} as 80 big-endian bits and appends 16 Crockford characters. */
  private static void appendRandomChars(
      final byte @NonNull [] b, final char @NonNull [] out, final int offset) {
    for (int i = 0; i < RANDOM_CHARS; i++) {
      final int bitStart = i * 5;
      int v = 0;
      for (int j = 0; j < 5; j++) {
        final int bit = bitStart + j;
        final int byteIdx = bit / 8;
        final int bitInByte = 7 - (bit % 8);
        v = (v << 1) | ((b[byteIdx] >> bitInByte) & 1);
      }
      out[offset + i] = CROCKFORD[v];
    }
  }
}
