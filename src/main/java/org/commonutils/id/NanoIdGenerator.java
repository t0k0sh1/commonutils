package org.commonutils.id;

import java.util.HashSet;
import java.util.Objects;
import java.util.random.RandomGenerator;
import java.util.random.RandomGeneratorFactory;
import org.commonutils.annotation.NonNull;
import org.commonutils.annotation.Positive;
import org.commonutils.internal.Contracts;

/**
 * {@link IdGenerator} for <a href="https://github.com/ai/nanoid">Nano ID</a>-style strings: a
 * random, URL-safe identifier using a fixed alphabet and length, with bias-free index selection
 * (reject masked values outside the alphabet range).
 *
 * <p>The no-argument constructor and constructors that omit a {@link RandomGenerator} use the same
 * cryptographically strong source as {@link UuidV7}. When a generator is supplied, thread safety
 * matches that of the given generator; if it is not thread-safe, callers must synchronize
 * externally or confine use to one thread.
 *
 * <p>Alphabets are validated: non-empty, BMP code points only (no supplementary characters or lone
 * surrogate code units), and each code point must appear at most once. Violations produce {@link
 * IllegalArgumentException}. Public constructors validate references with {@link
 * NullPointerException} when {@code null} is not permitted.
 *
 * @since 0.2.0
 */
public final class NanoIdGenerator implements IdGenerator<String> {

  /**
   * Default URL-safe alphabet from <a href="https://github.com/ai/nanoid">Nano ID</a> ({@value
   * #DEFAULT_ALPHABET_SIZE} characters, order preserved).
   */
  public static final @NonNull String DEFAULT_ALPHABET =
      "_-0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";

  /** Number of code points in {@link #DEFAULT_ALPHABET}. */
  public static final int DEFAULT_ALPHABET_SIZE = 64;

  /** Default string length (Nano ID default). */
  public static final int DEFAULT_SIZE = 21;

  private static final RandomGenerator SECURE_RANDOM =
      RandomGeneratorFactory.of("SecureRandom").create();

  private final char @NonNull [] alphabetChars;
  private final int mask;
  private final @Positive int size;
  private final @NonNull RandomGenerator random;

  /**
   * Uses {@link #DEFAULT_ALPHABET}, {@link #DEFAULT_SIZE}, and a cryptographically strong {@link
   * RandomGenerator}.
   *
   * @since 0.2.0
   */
  public NanoIdGenerator() {
    this(DEFAULT_ALPHABET, DEFAULT_SIZE, SECURE_RANDOM);
  }

  /**
   * Uses {@link #DEFAULT_ALPHABET} and {@link #DEFAULT_SIZE} with the supplied {@link
   * RandomGenerator}.
   *
   * @param randomGenerator source of random bytes, must not be {@code null}
   * @since 0.2.0
   */
  public NanoIdGenerator(final @NonNull RandomGenerator randomGenerator) {
    this(DEFAULT_ALPHABET, DEFAULT_SIZE, randomGenerator);
  }

  /**
   * Uses {@link #DEFAULT_ALPHABET} and the given length with the supplied {@link RandomGenerator}.
   *
   * @param size number of code points in each generated id, must be positive
   * @param randomGenerator source of random bytes, must not be {@code null}
   * @throws IllegalArgumentException if {@code size} is not positive
   * @since 0.2.0
   */
  public NanoIdGenerator(final @Positive int size, final @NonNull RandomGenerator randomGenerator) {
    this(DEFAULT_ALPHABET, size, randomGenerator);
  }

  /**
   * Uses the given alphabet and length with a cryptographically strong {@link RandomGenerator}.
   *
   * @param alphabet BMP code points only, unique, non-empty; must not be {@code null}
   * @param size number of characters in each generated id, must be positive
   * @throws NullPointerException if {@code alphabet} is {@code null}
   * @throws IllegalArgumentException if {@code alphabet} is empty, has duplicates, uses non-BMP or
   *     surrogate code points, or {@code size} is not positive
   * @since 0.2.0
   */
  public NanoIdGenerator(final @NonNull String alphabet, final @Positive int size) {
    this(alphabet, size, SECURE_RANDOM);
  }

  /**
   * Uses the given alphabet, length, and {@link RandomGenerator}.
   *
   * @param alphabet BMP code points only, unique, non-empty; must not be {@code null}
   * @param size number of characters in each generated id, must be positive
   * @param randomGenerator source of random bytes, must not be {@code null}
   * @throws NullPointerException if any reference argument is {@code null}
   * @throws IllegalArgumentException if {@code alphabet} is empty, has duplicates, uses non-BMP or
   *     surrogate code points, or {@code size} is not positive
   * @since 0.2.0
   */
  public NanoIdGenerator(
      final @NonNull String alphabet,
      final @Positive int size,
      final @NonNull RandomGenerator randomGenerator) {
    Objects.requireNonNull(alphabet, "alphabet");
    Objects.requireNonNull(randomGenerator, "randomGenerator");
    Contracts.requirePositive("size", size);
    alphabetChars = alphabetCharsFrom(alphabet);
    mask = maskForAlphabetLength(alphabetChars.length);
    this.size = size;
    random = randomGenerator;
  }

  /**
   * Generates a new Nano ID string using this generator's alphabet, length, and random source.
   *
   * @return a non-null string of length {@code size} whose code points are from the configured
   *     alphabet
   * @since 0.2.0
   */
  @Override
  public @NonNull String generate() {
    final char[] out = new char[size];
    final byte[] scratch = new byte[4];
    final int len = alphabetChars.length;
    for (int i = 0; i < size; i++) {
      out[i] = alphabetChars[nextIndex(random, scratch, mask, len)];
    }
    return new String(out);
  }

  private static char @NonNull [] alphabetCharsFrom(final String alphabet) {
    if (alphabet.isEmpty()) {
      throw new IllegalArgumentException("alphabet must not be empty");
    }
    final int[] cps = alphabet.codePoints().toArray();
    final HashSet<Integer> seen = new HashSet<>();
    final char[] chars = new char[cps.length];
    for (int i = 0; i < cps.length; i++) {
      final int cp = cps[i];
      if (cp > Character.MAX_VALUE) {
        throw new IllegalArgumentException("alphabet must use BMP code points only");
      }
      if (cp >= Character.MIN_SURROGATE && cp <= Character.MAX_SURROGATE) {
        throw new IllegalArgumentException("alphabet must not contain surrogate code points");
      }
      if (!seen.add(cp)) {
        throw new IllegalArgumentException("alphabet code points must be unique");
      }
      chars[i] = (char) cp;
    }
    return chars;
  }

  /** Same rule as Nano ID: smallest {@code (2^k - 1)} with {@code mask >= alphabetLength - 1}. */
  private static int maskForAlphabetLength(final int alphabetLength) {
    Contracts.requirePositive("alphabetLength", alphabetLength);
    final int clz = Integer.numberOfLeadingZeros((alphabetLength - 1) | 1);
    final int shift = 31 - clz;
    if (shift < 0 || shift >= 31) {
      throw new IllegalArgumentException("alphabetLength (" + alphabetLength + ") is too large");
    }
    return (2 << shift) - 1;
  }

  private static int nextIndex(
      final RandomGenerator rng, final byte[] scratch, final int mask, final int alphabetLength) {
    while (true) {
      rng.nextBytes(scratch);
      final int r =
          (scratch[0] & 0xFF)
              | ((scratch[1] & 0xFF) << 8)
              | ((scratch[2] & 0xFF) << 16)
              | ((scratch[3] & 0xFF) << 24);
      final int idx = r & mask;
      if (idx < alphabetLength) {
        return idx;
      }
    }
  }
}
