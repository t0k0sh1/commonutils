package org.commonutils.id;

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
 * <p>This class <strong>validates contracts</strong> on construction: non-null references with
 * {@link java.util.Objects#requireNonNull Object.requireNonNull}, positive {@code size}, and
 * alphabet rules as documented on each constructor.
 *
 * <p>Alphabets are validated: non-empty, BMP code points only (no supplementary characters or lone
 * surrogate code units), and each code point must appear at most once. Violations produce {@link
 * IllegalArgumentException}. Public constructors validate references with {@link
 * NullPointerException} when {@code null} is not permitted.
 *
 * <p><strong>API note:</strong> Constructors that default or omit an explicit {@link
 * RandomGenerator} use a cryptographically strong implementation ({@link
 * RandomGeneratorFactory}{@code .of("SecureRandom")}). Supplying a fast, predictable generator (for
 * example via {@link #nonCryptographic()}, {@link #nonCryptographic(int)}, or {@link
 * #nonCryptographic(String, int)}) is convenient for throughput or tests but is
 * <strong>unsuitable</strong> for secrets, session identifiers in hostile environments, or other
 * security-sensitive identifiers unless you deliberately accept weaker entropy.
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
    alphabetChars = NanoIdEncoding.alphabetCharsFrom(alphabet);
    mask = NanoIdEncoding.maskForAlphabetLength(alphabetChars.length);
    this.size = size;
    random = randomGenerator;
  }

  /**
   * Returns a generator that uses {@link #DEFAULT_ALPHABET}, {@link #DEFAULT_SIZE}, and a new
   * {@link java.util.SplittableRandom}-backed {@link RandomGenerator}. This prioritizes speed over
   * cryptographic unpredictability.
   *
   * @return a non-null generator; never {@code null}
   * @since 0.2.0
   */
  public static @NonNull NanoIdGenerator nonCryptographic() {
    return new NanoIdGenerator(DEFAULT_ALPHABET, DEFAULT_SIZE, splittableRandomGenerator());
  }

  /**
   * Returns a generator that uses {@link #DEFAULT_ALPHABET}, the given {@code size}, and a new
   * {@link java.util.SplittableRandom}-backed {@link RandomGenerator}.
   *
   * @param size number of code points per id, must be positive
   * @return a non-null generator
   * @throws IllegalArgumentException if {@code size} is not positive
   * @since 0.2.0
   */
  public static @NonNull NanoIdGenerator nonCryptographic(final @Positive int size) {
    Contracts.requirePositive("size", size);
    return new NanoIdGenerator(DEFAULT_ALPHABET, size, splittableRandomGenerator());
  }

  /**
   * Returns a generator that uses the given {@code alphabet} and {@code size} with a new {@link
   * java.util.SplittableRandom}-backed {@link RandomGenerator}.
   *
   * @param alphabet BMP code points only, unique, non-empty; must not be {@code null}
   * @param size number of characters per id, must be positive
   * @return a non-null generator
   * @throws NullPointerException if {@code alphabet} is {@code null}
   * @throws IllegalArgumentException if {@code alphabet} or {@code size} violates alphabet or size
   *     rules
   * @since 0.2.0
   */
  public static @NonNull NanoIdGenerator nonCryptographic(
      final @NonNull String alphabet, final @Positive int size) {
    Objects.requireNonNull(alphabet, "alphabet");
    Contracts.requirePositive("size", size);
    return new NanoIdGenerator(alphabet, size, splittableRandomGenerator());
  }

  private static @NonNull RandomGenerator splittableRandomGenerator() {
    return RandomGeneratorFactory.of("SplittableRandom").create();
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
    return NanoIdEncoding.generateNanoId(random, alphabetChars, mask, size);
  }
}
