package org.commonutils.id;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.random.RandomGenerator;
import java.util.random.RandomGeneratorFactory;
import org.junit.jupiter.api.Test;

class UlidIdGeneratorTest {

  @Test
  void fixedInstantSharesTimestampAcrossGenerate() {
    final Instant ins = Instant.ofEpochMilli(900L);
    final RandomGenerator rng = RandomGeneratorFactory.of("L128X256MixRandom").create(2026L);
    final UlidIdGenerator gen = new UlidIdGenerator(ins, rng);
    final String a = gen.generate();
    final String b = gen.generate();
    assertEquals(
        Ulid.encode(900L, new byte[Ulid.RANDOM_BYTES]).substring(0, Ulid.TIME_CHARS),
        a.substring(0, Ulid.TIME_CHARS));
    assertEquals(a.substring(0, Ulid.TIME_CHARS), b.substring(0, Ulid.TIME_CHARS));
    assertNotEquals(a, b);
  }

  @Test
  void clockBackedGeneratorReadsInstantEachCall() {
    final Clock stepping =
        new Clock() {
          private Instant i = Instant.ofEpochMilli(100L);

          @Override
          public java.time.ZoneId getZone() {
            return ZoneOffset.UTC;
          }

          @Override
          public Clock withZone(final java.time.ZoneId zone) {
            throw new UnsupportedOperationException();
          }

          @Override
          public Instant instant() {
            final Instant r = i;
            i = i.plusMillis(5L);
            return r;
          }
        };
    final UlidIdGenerator gen =
        new UlidIdGenerator(stepping, RandomGeneratorFactory.of("L128X256MixRandom").create(88L));
    assertEquals(
        Ulid.encode(100L, new byte[Ulid.RANDOM_BYTES]).substring(0, Ulid.TIME_CHARS),
        gen.generate().substring(0, Ulid.TIME_CHARS));
    assertEquals(
        Ulid.encode(105L, new byte[Ulid.RANDOM_BYTES]).substring(0, Ulid.TIME_CHARS),
        gen.generate().substring(0, Ulid.TIME_CHARS));
  }

  @Test
  void fixedClockProducesStableTimePrefixWithSecurePath() {
    final Instant frozen = Instant.ofEpochMilli(2468L);
    final Clock clock = Clock.fixed(frozen, ZoneOffset.UTC);
    final UlidIdGenerator gen = new UlidIdGenerator(clock);
    final String a = gen.generate();
    final String b = gen.generate();
    assertEquals(
        Ulid.encode(2468L, new byte[Ulid.RANDOM_BYTES]).substring(0, Ulid.TIME_CHARS),
        a.substring(0, Ulid.TIME_CHARS));
    assertEquals(a.substring(0, Ulid.TIME_CHARS), b.substring(0, Ulid.TIME_CHARS));
    assertNotEquals(a, b);
  }

  @Test
  void invalidInstantAtConstructionRejected() {
    assertThrows(
        IllegalArgumentException.class, () -> new UlidIdGenerator(Instant.ofEpochMilli(-5L)));
  }

  @Test
  void nullConstructorArgumentsRejected() {
    assertThrows(NullPointerException.class, () -> new UlidIdGenerator((Instant) null));
    assertThrows(NullPointerException.class, () -> new UlidIdGenerator(Instant.EPOCH, null));
    assertThrows(NullPointerException.class, () -> new UlidIdGenerator((Clock) null));
    assertThrows(NullPointerException.class, () -> new UlidIdGenerator(Clock.systemUTC(), null));
    assertThrows(NullPointerException.class, () -> UlidIdGenerator.nonCryptographic(null));
  }

  @Test
  void nonCryptographicProducesValidShape() {
    final UlidIdGenerator gen = UlidIdGenerator.nonCryptographic();
    final String s = gen.generate();
    assertEquals(Ulid.ENCODED_LENGTH, s.length());
    assertTrue(s.chars().allMatch(ch -> Ulid.CROCKFORD_BASE32.indexOf(ch) >= 0));
  }

  @Test
  void nonCryptographicWithFixedClockIsDeterministicAcrossInstancesPerGenerate() {
    final Clock clock = Clock.fixed(Instant.ofEpochMilli(4096L), ZoneOffset.UTC);
    final String a = UlidIdGenerator.nonCryptographic(clock).generate();
    final String b = UlidIdGenerator.nonCryptographic(clock).generate();
    assertEquals(
        Ulid.encode(4096L, new byte[Ulid.RANDOM_BYTES]).substring(0, Ulid.TIME_CHARS),
        a.substring(0, Ulid.TIME_CHARS));
    assertEquals(
        Ulid.encode(4096L, new byte[Ulid.RANDOM_BYTES]).substring(0, Ulid.TIME_CHARS),
        b.substring(0, Ulid.TIME_CHARS));
    assertNotEquals(a, b);
  }
}
