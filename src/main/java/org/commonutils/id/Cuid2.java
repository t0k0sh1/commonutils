package org.commonutils.id;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;
import java.util.random.RandomGenerator;
import java.util.regex.Pattern;
import org.commonutils.annotation.NonNull;

/**
 * Utilities and constants for <a href="https://github.com/paralleldrive/cuid2">Cuid2</a>-compatible
 * string identifiers ({@code SHA3-512}, base36, session counter, fingerprint). This class <strong>
 * validates contracts</strong> on public methods where stated.
 *
 * <p>The default fingerprint path matches the reference when no global object keys are available
 * (entropy-only {@code sourceString}). It does <em>not</em> reproduce Node.js fingerprints derived
 * from {@code Object.keys(global)}.
 *
 * @since 0.2.0
 */
public final class Cuid2 {

  /** Default output length (characters), matching {@code @paralleldrive/cuid2}. */
  public static final int DEFAULT_LENGTH = 24;

  /** Maximum output length ({@code bigLength} in the reference). */
  public static final int MAX_LENGTH = 32;

  /** Minimum supported output length (aligned with {@code isCuid} lower bound in the reference). */
  public static final int MIN_LENGTH = 2;

  /**
   * Exclusive upper bound for sampling the initial counter ({@code initialCountMax} in the
   * reference).
   */
  public static final int INITIAL_COUNT_MAX = 476782367;

  /**
   * Length of fingerprint strings produced by {@link #createDefaultFingerprint(RandomGenerator)}.
   */
  public static final int FINGERPRINT_LENGTH = 32;

  private static final Pattern CUID2_PATTERN = Pattern.compile("^[a-z][0-9a-z]+$");

  private Cuid2() {}

  /**
   * Returns whether {@code id} matches Cuid2 shape: first character {@code a-z}, remainder {@code
   * 0-9a-z}, length in [{@value #MIN_LENGTH}, {@value #MAX_LENGTH}].
   *
   * @param id candidate id, must not be {@code null}
   * @return {@code true} if the candidate matches
   * @throws NullPointerException if {@code id} is {@code null}
   * @since 0.2.0
   */
  public static boolean isValid(final @NonNull CharSequence id) {
    Objects.requireNonNull(id, "id");
    final int len = id.length();
    if (len < MIN_LENGTH || len > MAX_LENGTH) {
      return false;
    }
    return CUID2_PATTERN.matcher(id).matches();
  }

  /**
   * Builds a default fingerprint: {@code hash(createEntropy({@value #FINGERPRINT_LENGTH},
   * rng)).substring(0, {@value #FINGERPRINT_LENGTH})}.
   *
   * @param rng entropy source, must not be {@code null}
   * @return a non-null fingerprint string of length {@value #FINGERPRINT_LENGTH}
   * @throws NullPointerException if {@code rng} is {@code null}
   * @since 0.2.0
   */
  public static @NonNull String createDefaultFingerprint(final @NonNull RandomGenerator rng) {
    Objects.requireNonNull(rng, "randomGenerator");
    final String source = createEntropy(FINGERPRINT_LENGTH, rng);
    final String hashed = hash(source);
    return truncateFingerprint(hashed);
  }

  /**
   * One Cuid2-shaped id (see {@link Cuid2IdGenerator}). Public for tests; production code typically
   * uses {@link Cuid2IdGenerator}.
   *
   * @param length output length ({@value #MIN_LENGTH}–{@value #MAX_LENGTH})
   * @param rng random source, must not be {@code null}
   * @param clock clock for Unix milliseconds, must not be {@code null}
   * @param fingerprint concatenated into the hash input (reference uses a 32-character default);
   *     must not be {@code null} or empty
   * @param counter session counter ({@link AtomicLong#getAndIncrement} per call)
   * @return a non-null id of length {@code length}
   * @throws NullPointerException if any reference argument is {@code null}
   * @throws IllegalArgumentException if {@code length} is out of range, {@code fingerprint} is
   *     empty, or clock millis are not representable as for UUID v7
   * @since 0.2.0
   */
  public static @NonNull String generate(
      final int length,
      final @NonNull RandomGenerator rng,
      final @NonNull Clock clock,
      final @NonNull String fingerprint,
      final @NonNull AtomicLong counter) {
    if (length < MIN_LENGTH || length > MAX_LENGTH) {
      throw new IllegalArgumentException(
          "length ("
              + length
              + ") must be between "
              + MIN_LENGTH
              + " and "
              + MAX_LENGTH
              + " inclusive");
    }
    Objects.requireNonNull(rng, "randomGenerator");
    Objects.requireNonNull(clock, "clock");
    Objects.requireNonNull(fingerprint, "fingerprint");
    Objects.requireNonNull(counter, "counter");
    if (fingerprint.isEmpty()) {
      throw new IllegalArgumentException("fingerprint must not be empty");
    }
    final long ms = UuidV7.requireUnixMillis(clock.instant());
    final String time = Long.toString(ms, 36);
    final char firstLetter = randomLetter(rng);
    final String salt = createEntropy(length, rng);
    final String countStr = Long.toString(counter.getAndIncrement(), 36);
    final String hashInput = time + salt + countStr + fingerprint;
    final String hashed = hash(hashInput);
    if (hashed.length() < length) {
      throw new IllegalStateException("hash output too short for length " + length);
    }
    return firstLetter + hashed.substring(1, length);
  }

  static @NonNull String hash(final @NonNull String utf8Input) {
    Objects.requireNonNull(utf8Input, "utf8Input");
    final MessageDigest md;
    try {
      md = MessageDigest.getInstance("SHA3-512");
    } catch (final NoSuchAlgorithmException e) {
      throw new IllegalStateException("SHA3-512 not available", e);
    }
    final byte[] digest = md.digest(utf8Input.getBytes(StandardCharsets.UTF_8));
    final String base36 = new BigInteger(1, digest).toString(36);
    if (base36.length() <= 1) {
      return "";
    }
    return base36.substring(1);
  }

  static @NonNull String createEntropy(final int minLength, final @NonNull RandomGenerator rng) {
    if (minLength < 0) {
      throw new IllegalArgumentException("minLength must be non-negative");
    }
    Objects.requireNonNull(rng, "randomGenerator");
    final StringBuilder entropy = new StringBuilder(minLength);
    while (entropy.length() < minLength) {
      entropy.append(Integer.toString(rng.nextInt(36), 36));
    }
    return entropy.toString();
  }

  static char randomLetter(final @NonNull RandomGenerator rng) {
    Objects.requireNonNull(rng, "randomGenerator");
    return (char) ('a' + rng.nextInt(26));
  }

  private static @NonNull String truncateFingerprint(final @NonNull String hashed) {
    if (hashed.length() <= FINGERPRINT_LENGTH) {
      return hashed;
    }
    return hashed.substring(0, FINGERPRINT_LENGTH);
  }
}
