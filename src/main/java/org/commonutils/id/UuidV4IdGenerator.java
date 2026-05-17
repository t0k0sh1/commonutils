package org.commonutils.id;

import java.util.Objects;
import java.util.UUID;
import java.util.random.RandomGenerator;
import org.commonutils.annotation.NonNull;
import org.commonutils.annotation.Nullable;

/**
 * {@link IdGenerator} for <a href="https://www.rfc-editor.org/rfc/rfc4122">RFC 4122</a> version-4
 * (random) UUIDs.
 *
 * <p>The no-argument constructor delegates to {@link UUID#randomUUID()}, using the JDK's
 * cryptographically strong source. The {@link RandomGenerator} constructor builds version-4 UUIDs
 * from 16 random bytes produced by the supplied generator; it validates the parameter (see below)
 * and is intended for tests and custom entropy sources.
 *
 * <p><strong>Thread safety</strong>: the no-argument form is safe for concurrent use. When a {@link
 * RandomGenerator} is supplied, thread safety matches that of the given generator; if it is not
 * thread-safe, callers must synchronize externally or confine use to one thread.
 *
 * <p>Public constructors validate references with {@link NullPointerException} when {@code null} is
 * not permitted.
 *
 * @since 0.2.0
 */
public final class UuidV4IdGenerator implements IdGenerator<UUID> {

  private final @Nullable RandomGenerator randomGenerator;

  /** Uses {@link UUID#randomUUID()} on each {@link #generate()} call. */
  public UuidV4IdGenerator() {
    randomGenerator = null;
  }

  /**
   * Uses the given generator to produce 16 random bytes per {@link #generate()} call, applying RFC
   * 4122 version-4 and variant bits.
   *
   * @param randomGenerator source of random bytes, must not be {@code null}
   */
  public UuidV4IdGenerator(final @NonNull RandomGenerator randomGenerator) {
    this.randomGenerator = Objects.requireNonNull(randomGenerator, "randomGenerator");
  }

  @Override
  public @NonNull UUID generate() {
    final RandomGenerator local = randomGenerator;
    if (local == null) {
      return UUID.randomUUID();
    }
    return randomUuidV4(local);
  }

  private static @NonNull UUID randomUuidV4(final RandomGenerator random) {
    final byte[] bytes = new byte[16];
    random.nextBytes(bytes);
    // version 4: 0100 in the version field (time_hi_and_version, bits 12-15)
    bytes[6] &= 0x0f;
    bytes[6] |= 0x40;
    // RFC 4122 variant: two most significant bits of clock_seq_hi_and_reserved are 10
    bytes[8] &= 0x3f;
    bytes[8] |= (byte) 0x80;
    long msb = 0L;
    long lsb = 0L;
    for (int i = 0; i < 8; i++) {
      msb = (msb << 8) | (bytes[i] & 0xffL);
    }
    for (int i = 8; i < 16; i++) {
      lsb = (lsb << 8) | (bytes[i] & 0xffL);
    }
    return new UUID(msb, lsb);
  }
}
