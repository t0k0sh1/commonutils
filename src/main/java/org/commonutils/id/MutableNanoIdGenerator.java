package org.commonutils.id;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.random.RandomGenerator;
import java.util.random.RandomGeneratorFactory;
import org.commonutils.annotation.NonNull;
import org.commonutils.annotation.Positive;
import org.commonutils.internal.Contracts;

/**
 * {@link IdGenerator} for mutable <a href="https://github.com/ai/nanoid">Nano ID</a>-style strings:
 * alphabet and length can be changed after construction while sharing one {@link RandomGenerator}.
 *
 * <p>Each {@link #generate()} call reads the current configuration atomically as a snapshot, so a
 * single id uses one consistent alphabet and length even if another thread updates configuration
 * midway. Updating configuration uses compare-and-set loops so concurrent {@link #setAlphabet},
 * {@link #setSize}, and {@link #configure} calls compose without dropping unrelated fields (for
 * example {@link #setAlphabet} preserves the latest observed {@code size}).
 *
 * <p>The no-argument constructor and constructors that omit a {@link RandomGenerator} use the same
 * cryptographically strong source as {@link UuidV7}. Thread safety of random draws follows the
 * supplied {@link RandomGenerator}; if it is not thread-safe, callers must synchronize externally
 * or confine use to one thread.
 *
 * <p>This class <strong>validates contracts</strong> on construction and on configuration mutators:
 * non-null references with {@link java.util.Objects#requireNonNull Object.requireNonNull}, positive
 * {@code size}, and alphabet rules aligned with {@link NanoIdGenerator}. Violations produce {@link
 * NullPointerException} or {@link IllegalArgumentException} as documented per method.
 *
 * <p><strong>API note:</strong> Prefer {@link #configure(String, int)} when changing both alphabet
 * and length so callers never observe transient combinations of old alphabet with new length (or
 * vice versa). Static {@code nonCryptographic…} factories create a fast {@link
 * java.util.SplittableRandom}-backed generator and are unsuitable for security-sensitive
 * identifiers for the same reasons as {@link NanoIdGenerator#nonCryptographic()}.
 *
 * @since 0.2.0
 */
public final class MutableNanoIdGenerator implements IdGenerator<String> {

  private static final RandomGenerator SECURE_RANDOM =
      RandomGeneratorFactory.of("SecureRandom").create();

  private record Snapshot(char[] alphabetChars, int mask, int size) {}

  private final AtomicReference<Snapshot> snapshot;
  private final RandomGenerator random;

  /**
   * Uses {@link NanoIdGenerator#DEFAULT_ALPHABET}, {@link NanoIdGenerator#DEFAULT_SIZE}, and a
   * cryptographically strong {@link RandomGenerator}.
   *
   * @since 0.2.0
   */
  public MutableNanoIdGenerator() {
    this(NanoIdGenerator.DEFAULT_ALPHABET, NanoIdGenerator.DEFAULT_SIZE, SECURE_RANDOM);
  }

  /**
   * Uses {@link NanoIdGenerator#DEFAULT_ALPHABET} and {@link NanoIdGenerator#DEFAULT_SIZE} with the
   * supplied {@link RandomGenerator}.
   *
   * @param randomGenerator entropy source; must not be {@code null}
   * @since 0.2.0
   */
  public MutableNanoIdGenerator(final @NonNull RandomGenerator randomGenerator) {
    this(NanoIdGenerator.DEFAULT_ALPHABET, NanoIdGenerator.DEFAULT_SIZE, randomGenerator);
  }

  /**
   * Uses {@link NanoIdGenerator#DEFAULT_ALPHABET} and the given length with the supplied {@link
   * RandomGenerator}.
   *
   * @param size number of code points per id; must be positive
   * @param randomGenerator entropy source; must not be {@code null}
   * @throws IllegalArgumentException if {@code size} is not positive
   * @since 0.2.0
   */
  public MutableNanoIdGenerator(
      final @Positive int size, final @NonNull RandomGenerator randomGenerator) {
    this(NanoIdGenerator.DEFAULT_ALPHABET, size, randomGenerator);
  }

  /**
   * Uses the given alphabet and length with a cryptographically strong {@link RandomGenerator}.
   *
   * @param alphabet BMP code points only, unique, non-empty; must not be {@code null}
   * @param size number of characters per id; must be positive
   * @throws NullPointerException if {@code alphabet} is {@code null}
   * @throws IllegalArgumentException if {@code alphabet} or {@code size} violates alphabet or size
   *     rules
   * @since 0.2.0
   */
  public MutableNanoIdGenerator(final @NonNull String alphabet, final @Positive int size) {
    this(alphabet, size, SECURE_RANDOM);
  }

  /**
   * Uses the given alphabet, length, and {@link RandomGenerator}.
   *
   * @param alphabet BMP code points only, unique, non-empty; must not be {@code null}
   * @param size number of characters per id; must be positive
   * @param randomGenerator entropy source; must not be {@code null}
   * @throws NullPointerException if any reference argument is {@code null}
   * @throws IllegalArgumentException if {@code alphabet} or {@code size} violates alphabet or size
   *     rules
   * @since 0.2.0
   */
  public MutableNanoIdGenerator(
      final @NonNull String alphabet,
      final @Positive int size,
      final @NonNull RandomGenerator randomGenerator) {
    Objects.requireNonNull(alphabet, "alphabet");
    Objects.requireNonNull(randomGenerator, "randomGenerator");
    Contracts.requirePositive("size", size);
    random = randomGenerator;
    snapshot = new AtomicReference<>(snapshotFromAlphabetAndSize(alphabet, size));
  }

  /**
   * Returns a mutable generator using defaults with a new {@link java.util.SplittableRandom}-backed
   * {@link RandomGenerator}.
   *
   * @return a non-null generator
   * @since 0.2.0
   */
  public static @NonNull MutableNanoIdGenerator nonCryptographic() {
    return new MutableNanoIdGenerator(
        NanoIdGenerator.DEFAULT_ALPHABET,
        NanoIdGenerator.DEFAULT_SIZE,
        splittableRandomGenerator());
  }

  /**
   * Returns a mutable generator using {@link NanoIdGenerator#DEFAULT_ALPHABET}, {@code size}, and a
   * new {@link java.util.SplittableRandom}-backed {@link RandomGenerator}.
   *
   * @param size number of code points per id; must be positive
   * @return a non-null generator
   * @throws IllegalArgumentException if {@code size} is not positive
   * @since 0.2.0
   */
  public static @NonNull MutableNanoIdGenerator nonCryptographic(final @Positive int size) {
    Contracts.requirePositive("size", size);
    return new MutableNanoIdGenerator(
        NanoIdGenerator.DEFAULT_ALPHABET, size, splittableRandomGenerator());
  }

  /**
   * Returns a mutable generator using {@code alphabet}, {@code size}, and a new {@link
   * java.util.SplittableRandom}-backed {@link RandomGenerator}.
   *
   * @param alphabet BMP alphabet string; must not be {@code null}
   * @param size length per id; must be positive
   * @return a non-null generator
   * @throws NullPointerException if {@code alphabet} is {@code null}
   * @throws IllegalArgumentException if validation fails
   * @since 0.2.0
   */
  public static @NonNull MutableNanoIdGenerator nonCryptographic(
      final @NonNull String alphabet, final @Positive int size) {
    Objects.requireNonNull(alphabet, "alphabet");
    Contracts.requirePositive("size", size);
    return new MutableNanoIdGenerator(alphabet, size, splittableRandomGenerator());
  }

  private static @NonNull RandomGenerator splittableRandomGenerator() {
    return RandomGeneratorFactory.of("SplittableRandom").create();
  }

  /**
   * Atomically replaces alphabet and length.
   *
   * @param alphabet BMP code points only, unique, non-empty; must not be {@code null}
   * @param size number of characters per id; must be positive
   * @throws NullPointerException if {@code alphabet} is {@code null}
   * @throws IllegalArgumentException if validation fails
   * @since 0.2.0
   */
  public void configure(final @NonNull String alphabet, final @Positive int size) {
    Objects.requireNonNull(alphabet, "alphabet");
    Contracts.requirePositive("size", size);
    snapshot.set(snapshotFromAlphabetAndSize(alphabet, size));
  }

  /**
   * Updates the alphabet while preserving the latest observed {@code size}. Matches concurrent
   * updates via compare-and-set.
   *
   * @param alphabet BMP code points only, unique, non-empty; must not be {@code null}
   * @throws NullPointerException if {@code alphabet} is {@code null}
   * @throws IllegalArgumentException if alphabet validation fails
   * @since 0.2.0
   */
  public void setAlphabet(final @NonNull String alphabet) {
    Objects.requireNonNull(alphabet, "alphabet");
    final char[] chars = NanoIdEncoding.alphabetCharsFrom(alphabet);
    final int mask = NanoIdEncoding.maskForAlphabetLength(chars.length);
    while (true) {
      final Snapshot cur = snapshot.get();
      final Snapshot next = new Snapshot(chars, mask, cur.size());
      if (snapshot.compareAndSet(cur, next)) {
        return;
      }
    }
  }

  /**
   * Updates {@code size} while preserving the latest observed alphabet and mask.
   *
   * @param size number of characters per id; must be positive
   * @throws IllegalArgumentException if {@code size} is not positive
   * @since 0.2.0
   */
  public void setSize(final @Positive int size) {
    Contracts.requirePositive("size", size);
    while (true) {
      final Snapshot cur = snapshot.get();
      final Snapshot next = new Snapshot(cur.alphabetChars(), cur.mask(), size);
      if (snapshot.compareAndSet(cur, next)) {
        return;
      }
    }
  }

  /**
   * Generates an id using the configuration observed at invocation start.
   *
   * @return a non-null string satisfying the snapshot alphabet and length
   * @since 0.2.0
   */
  @Override
  public @NonNull String generate() {
    final Snapshot snap = snapshot.get();
    return NanoIdEncoding.generateNanoId(random, snap.alphabetChars(), snap.mask(), snap.size());
  }

  private static Snapshot snapshotFromAlphabetAndSize(final String alphabet, final int size) {
    final char[] chars = NanoIdEncoding.alphabetCharsFrom(alphabet);
    final int mask = NanoIdEncoding.maskForAlphabetLength(chars.length);
    return new Snapshot(chars, mask, size);
  }
}
