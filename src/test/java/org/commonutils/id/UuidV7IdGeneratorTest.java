package org.commonutils.id;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;
import java.util.random.RandomGenerator;
import java.util.random.RandomGeneratorFactory;
import org.junit.jupiter.api.Test;

class UuidV7IdGeneratorTest {

  @Test
  void fixedInstantSharesTimestampAcrossGenerate() {
    final Instant ins = Instant.ofEpochMilli(900L);
    final RandomGenerator rng = RandomGeneratorFactory.of("L128X256MixRandom").create(2026L);
    final UuidV7IdGenerator gen = new UuidV7IdGenerator(ins, rng);
    final UUID a = gen.generate();
    final UUID b = gen.generate();
    assertEquals(7, a.version());
    assertEquals(900L, a.getMostSignificantBits() >>> 16);
    assertEquals(900L, b.getMostSignificantBits() >>> 16);
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
    final RandomGenerator rng = RandomGeneratorFactory.of("L128X256MixRandom").create(88L);
    final UuidV7IdGenerator gen = new UuidV7IdGenerator(stepping, rng);
    assertEquals(100L, gen.generate().getMostSignificantBits() >>> 16);
    assertEquals(105L, gen.generate().getMostSignificantBits() >>> 16);
  }

  @Test
  void fixedClockProducesStableEmbeddedMillisWithSecurePath() {
    final Instant frozen = Instant.ofEpochMilli(2468L);
    final Clock clock = Clock.fixed(frozen, ZoneOffset.UTC);
    final UuidV7IdGenerator gen = new UuidV7IdGenerator(clock);
    assertEquals(2468L, gen.generate().getMostSignificantBits() >>> 16);
    assertEquals(2468L, gen.generate().getMostSignificantBits() >>> 16);
  }

  @Test
  void invalidInstantAtConstructionRejected() {
    assertThrows(
        IllegalArgumentException.class, () -> new UuidV7IdGenerator(Instant.ofEpochMilli(-5L)));
  }

  @Test
  void nullConstructorArgumentsRejected() {
    assertThrows(NullPointerException.class, () -> new UuidV7IdGenerator((Instant) null));
    assertThrows(NullPointerException.class, () -> new UuidV7IdGenerator(Instant.EPOCH, null));
    assertThrows(NullPointerException.class, () -> new UuidV7IdGenerator((Clock) null));
    assertThrows(NullPointerException.class, () -> new UuidV7IdGenerator(Clock.systemUTC(), null));
  }

  @Test
  void deterministicWithSeededRandom() {
    final Instant ins = Instant.ofEpochMilli(333L);
    final long seed = 0xBEEFL;
    final RandomGenerator a = RandomGeneratorFactory.of("L128X256MixRandom").create(seed);
    final RandomGenerator b = RandomGeneratorFactory.of("L128X256MixRandom").create(seed);
    assertEquals(
        new UuidV7IdGenerator(ins, a).generate(), new UuidV7IdGenerator(ins, b).generate());
  }

  @Test
  void versionAndVariant() {
    final UUID id = new UuidV7IdGenerator(Instant.ofEpochMilli(1L), seededRng(1L)).generate();
    assertEquals(7, id.version());
    assertTrue(id.variant() == 2);
  }

  private static RandomGenerator seededRng(final long seed) {
    return RandomGeneratorFactory.of("L128X256MixRandom").create(seed);
  }
}
