package org.commonutils.id;

import java.time.Clock;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;
import java.util.random.RandomGenerator;
import java.util.random.RandomGeneratorFactory;
import org.commonutils.annotation.NonNull;

/**
 * {@link IdGenerator} for <a href="https://github.com/paralleldrive/cuid2">Cuid2</a>-compatible
 * strings: {@code SHA3-512} over UTF-8 inputs, base36, a per-instance session counter, a fixed
 * fingerprint (entropy-derived by default), and a leading random lowercase letter. Each {@link
 * #generate()} reads {@link Clock#instant()} for Unix milliseconds ({@link
 * org.commonutils.id.UuidV7}-compatible bounds).
 *
 * <p>The default fingerprint matches the reference when no global object keys exist (see {@link
 * Cuid2#createDefaultFingerprint(RandomGenerator)}). It will not match Node.js fingerprints built
 * from {@code Object.keys(global)}.
 *
 * <p>Cuid2 is intentionally <strong>not k-sortable</strong>; use {@link Ulid} / {@link
 * UlidIdGenerator} or {@link UuidV7} when you need coarse time order in encoded form.
 *
 * <p>The no-argument and secure-default constructors use {@link RandomGeneratorFactory}{@code
 * .of("SecureRandom")}. When a generator is supplied, thread safety matches that of the given
 * generator; if it is not thread-safe, callers must synchronize externally or confine use to one
 * thread.
 *
 * <p>Static {@link #nonCryptographic()} and {@link #nonCryptographic(int)} use a {@link
 * java.util.SplittableRandom}-backed {@link RandomGenerator}; see the API note below.
 *
 * <p><strong>API note:</strong> {@link #nonCryptographic()} methods prioritize speed over
 * cryptographic unpredictability and are <strong>unsuitable</strong> for secrets, session
 * identifiers in hostile environments, or other security-sensitive identifiers unless you
 * deliberately accept weaker entropy.
 *
 * <p>This class <strong>validates contracts</strong> on construction and rejects empty custom
 * fingerprints with {@link IllegalArgumentException}.
 *
 * <p>Public constructors validate references with {@link NullPointerException} when {@code null} is
 * not permitted.
 *
 * @since 0.2.0
 */
public final class Cuid2IdGenerator implements IdGenerator<String> {

  private static final @NonNull RandomGenerator SECURE_RANDOM =
      RandomGeneratorFactory.of("SecureRandom").create();

  private final int length;
  private final @NonNull RandomGenerator random;
  private final @NonNull Clock clock;
  private final @NonNull String fingerprint;
  private final @NonNull AtomicLong counter;

  /**
   * Uses {@link Cuid2#DEFAULT_LENGTH}, {@link Clock#systemUTC()}, and a cryptographically strong
   * {@link RandomGenerator}.
   *
   * @since 0.2.0
   */
  public Cuid2IdGenerator() {
    this(Cuid2.DEFAULT_LENGTH, SECURE_RANDOM, Clock.systemUTC());
  }

  /**
   * Uses {@link Clock#systemUTC()} and a cryptographically strong {@link RandomGenerator}.
   *
   * @param length output length ({@value Cuid2#MIN_LENGTH}–{@value Cuid2#MAX_LENGTH})
   * @throws IllegalArgumentException if {@code length} is out of range
   * @since 0.2.0
   */
  public Cuid2IdGenerator(final int length) {
    this(length, SECURE_RANDOM, Clock.systemUTC());
  }

  /**
   * Uses the given length, {@link RandomGenerator}, and {@link Clock#systemUTC()}.
   *
   * @param length output length ({@value Cuid2#MIN_LENGTH}–{@value Cuid2#MAX_LENGTH})
   * @param randomGenerator entropy source, must not be {@code null}
   * @throws NullPointerException if {@code randomGenerator} is {@code null}
   * @throws IllegalArgumentException if {@code length} is out of range
   * @since 0.2.0
   */
  public Cuid2IdGenerator(final int length, final @NonNull RandomGenerator randomGenerator) {
    this(length, randomGenerator, Clock.systemUTC());
  }

  /**
   * Uses the given length, {@link RandomGenerator}, and {@link Clock}. The initial counter sample
   * and default fingerprint are drawn from {@code randomGenerator} in the same order as {@code
   * init} in the reference implementation.
   *
   * @param length output length ({@value Cuid2#MIN_LENGTH}–{@value Cuid2#MAX_LENGTH})
   * @param randomGenerator entropy source, must not be {@code null}
   * @param clock clock for Unix milliseconds, must not be {@code null}
   * @throws NullPointerException if any argument is {@code null}
   * @throws IllegalArgumentException if {@code length} is out of range
   * @since 0.2.0
   */
  public Cuid2IdGenerator(
      final int length,
      final @NonNull RandomGenerator randomGenerator,
      final @NonNull Clock clock) {
    Cuid2IdGenerator.requireLength(length);
    this.length = length;
    random = Objects.requireNonNull(randomGenerator, "randomGenerator");
    this.clock = Objects.requireNonNull(clock, "clock");
    counter = new AtomicLong(random.nextInt(Cuid2.INITIAL_COUNT_MAX));
    fingerprint = Cuid2.createDefaultFingerprint(random);
  }

  /**
   * Uses a caller-supplied fingerprint (for example to match a deployment-specific value) with the
   * given length, {@link RandomGenerator}, and {@link Clock}. The initial counter value is {@code
   * floor(random * }{@link Cuid2#INITIAL_COUNT_MAX}{@code )} per the reference.
   *
   * @param length output length ({@value Cuid2#MIN_LENGTH}–{@value Cuid2#MAX_LENGTH})
   * @param randomGenerator entropy source, must not be {@code null}
   * @param clock clock for Unix milliseconds, must not be {@code null}
   * @param fingerprint non-empty string concatenated into the hash input, must not be {@code null}
   * @throws NullPointerException if any argument is {@code null}
   * @throws IllegalArgumentException if {@code length} is out of range or {@code fingerprint} is
   *     empty
   * @since 0.2.0
   */
  public Cuid2IdGenerator(
      final int length,
      final @NonNull RandomGenerator randomGenerator,
      final @NonNull Clock clock,
      final @NonNull String fingerprint) {
    Cuid2IdGenerator.requireLength(length);
    this.length = length;
    random = Objects.requireNonNull(randomGenerator, "randomGenerator");
    this.clock = Objects.requireNonNull(clock, "clock");
    Objects.requireNonNull(fingerprint, "fingerprint");
    if (fingerprint.isEmpty()) {
      throw new IllegalArgumentException("fingerprint must not be empty");
    }
    counter = new AtomicLong(random.nextInt(Cuid2.INITIAL_COUNT_MAX));
    this.fingerprint = fingerprint;
  }

  /**
   * Uses a caller-supplied fingerprint and explicit initial counter (for deterministic tests). Does
   * not draw an initial-counter sample from {@code randomGenerator}.
   *
   * @param length output length ({@value Cuid2#MIN_LENGTH}–{@value Cuid2#MAX_LENGTH})
   * @param randomGenerator entropy source for letters and salt, must not be {@code null}
   * @param clock clock for Unix milliseconds, must not be {@code null}
   * @param fingerprint non-empty string concatenated into the hash input, must not be {@code null}
   * @param initialCounter first value returned by the session counter's {@code getAndIncrement}
   * @throws NullPointerException if any reference argument is {@code null}
   * @throws IllegalArgumentException if {@code length} is out of range or {@code fingerprint} is
   *     empty
   * @since 0.2.0
   */
  public Cuid2IdGenerator(
      final int length,
      final @NonNull RandomGenerator randomGenerator,
      final @NonNull Clock clock,
      final @NonNull String fingerprint,
      final long initialCounter) {
    Cuid2IdGenerator.requireLength(length);
    this.length = length;
    random = Objects.requireNonNull(randomGenerator, "randomGenerator");
    this.clock = Objects.requireNonNull(clock, "clock");
    Objects.requireNonNull(fingerprint, "fingerprint");
    if (fingerprint.isEmpty()) {
      throw new IllegalArgumentException("fingerprint must not be empty");
    }
    counter = new AtomicLong(initialCounter);
    this.fingerprint = fingerprint;
  }

  /**
   * Returns a generator with {@link Cuid2#DEFAULT_LENGTH}, {@link Clock#systemUTC()}, and a new
   * {@link java.util.SplittableRandom}-backed {@link RandomGenerator}.
   *
   * @return a non-null generator
   * @since 0.2.0
   */
  public static @NonNull Cuid2IdGenerator nonCryptographic() {
    return new Cuid2IdGenerator(
        Cuid2.DEFAULT_LENGTH, splittableRandomGenerator(), Clock.systemUTC());
  }

  /**
   * Returns a generator with the given length, {@link Clock#systemUTC()}, and a new {@link
   * java.util.SplittableRandom}-backed {@link RandomGenerator}.
   *
   * @param length output length ({@value Cuid2#MIN_LENGTH}–{@value Cuid2#MAX_LENGTH})
   * @return a non-null generator
   * @throws IllegalArgumentException if {@code length} is out of range
   * @since 0.2.0
   */
  public static @NonNull Cuid2IdGenerator nonCryptographic(final int length) {
    Cuid2IdGenerator.requireLength(length);
    return new Cuid2IdGenerator(length, splittableRandomGenerator(), Clock.systemUTC());
  }

  private static @NonNull RandomGenerator splittableRandomGenerator() {
    return RandomGeneratorFactory.of("SplittableRandom").create();
  }

  private static void requireLength(final int length) {
    if (length < Cuid2.MIN_LENGTH || length > Cuid2.MAX_LENGTH) {
      throw new IllegalArgumentException(
          "length ("
              + length
              + ") must be between "
              + Cuid2.MIN_LENGTH
              + " and "
              + Cuid2.MAX_LENGTH
              + " inclusive");
    }
  }

  /**
   * Generates a new Cuid2-compatible id.
   *
   * @return a non-null string of length configured at construction
   * @throws IllegalArgumentException if the clock instant is not representable as a 48-bit unsigned
   *     Unix timestamp
   * @since 0.2.0
   */
  @Override
  public @NonNull String generate() {
    return Cuid2.generate(length, random, clock, fingerprint, counter);
  }
}
