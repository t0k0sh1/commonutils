package org.commonutils.id;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.random.RandomGenerator;
import java.util.random.RandomGeneratorFactory;
import org.junit.jupiter.api.Test;

class UlidTest {

  @Test
  void encodeZeroTimeZeroRandomIsAllZeros() {
    final byte[] r = new byte[Ulid.RANDOM_BYTES];
    assertEquals("00000000000000000000000000", Ulid.encode(0L, r));
  }

  @Test
  void encodeOneMillisecondZeroRandomMatchesVector() {
    final byte[] r = new byte[Ulid.RANDOM_BYTES];
    assertEquals("00000000040000000000000000", Ulid.encode(1L, r));
  }

  @Test
  void encodeZeroTimeAllOnesRandomIsMaxCrockfordRun() {
    final byte[] r = new byte[Ulid.RANDOM_BYTES];
    for (int i = 0; i < r.length; i++) {
      r[i] = (byte) 0xFF;
    }
    assertEquals("0000000000ZZZZZZZZZZZZZZZZ", Ulid.encode(0L, r));
  }

  @Test
  void encodeTimestamp1484841461104MatchesTimePrefix() {
    final byte[] r = new byte[Ulid.RANDOM_BYTES];
    assertEquals("00ASPXSAA00000000000000000", Ulid.encode(1_484_841_461_104L, r));
  }

  @Test
  void encodeRejectsNegativeMillis() {
    final byte[] r = new byte[Ulid.RANDOM_BYTES];
    assertThrows(IllegalArgumentException.class, () -> Ulid.encode(-1L, r));
  }

  @Test
  void encodeRejectsMillisAbove48Bits() {
    final byte[] r = new byte[Ulid.RANDOM_BYTES];
    assertThrows(IllegalArgumentException.class, () -> Ulid.encode(UuidV7.MAX_UNIX_TS_MS + 1L, r));
  }

  @Test
  void encodeRejectsWrongRandomLength() {
    assertThrows(
        IllegalArgumentException.class, () -> Ulid.encode(0L, new byte[Ulid.RANDOM_BYTES - 1]));
    assertThrows(
        IllegalArgumentException.class, () -> Ulid.encode(0L, new byte[Ulid.RANDOM_BYTES + 1]));
  }

  @Test
  void encodeRejectsNullRandomness() {
    assertThrows(NullPointerException.class, () -> Ulid.encode(0L, null));
  }

  @Test
  void generateFromInstantHasExpectedShape() {
    final RandomGenerator rng = RandomGeneratorFactory.of("L128X256MixRandom").create(42L);
    final String s = Ulid.fromInstant(Instant.ofEpochMilli(99L), rng);
    assertEquals(Ulid.ENCODED_LENGTH, s.length());
    assertTrue(s.chars().allMatch(ch -> Ulid.CROCKFORD_BASE32.indexOf(ch) >= 0));
    for (int i = 0; i < s.length(); i++) {
      assertTrue(s.charAt(i) == Character.toUpperCase(s.charAt(i)));
    }
  }

  @Test
  void deterministicFromInstantMatchesEncode() {
    final RandomGenerator rng = RandomGeneratorFactory.of("L128X256MixRandom").create(99L);
    final byte[] buf = new byte[Ulid.RANDOM_BYTES];
    rng.nextBytes(buf);
    final RandomGenerator rng2 = RandomGeneratorFactory.of("L128X256MixRandom").create(99L);
    final String a = Ulid.fromInstant(Instant.ofEpochMilli(1234L), rng2);
    final String b = Ulid.encode(1234L, buf);
    assertEquals(b, a);
  }

  @Test
  void fromClockStepsWithClock() {
    final Clock stepping =
        new Clock() {
          private Instant i = Instant.ofEpochMilli(10L);

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
            i = i.plusMillis(3L);
            return r;
          }
        };
    assertEquals(
        Ulid.fromInstant(
            Instant.ofEpochMilli(10L), RandomGeneratorFactory.of("L128X256MixRandom").create(7L)),
        Ulid.fromClock(stepping, RandomGeneratorFactory.of("L128X256MixRandom").create(7L)));
    assertEquals(
        Ulid.fromInstant(
            Instant.ofEpochMilli(13L), RandomGeneratorFactory.of("L128X256MixRandom").create(7L)),
        Ulid.fromClock(stepping, RandomGeneratorFactory.of("L128X256MixRandom").create(7L)));
  }

  @Test
  void fromInstantRejectsNulls() {
    assertThrows(NullPointerException.class, () -> Ulid.fromInstant(null));
    assertThrows(NullPointerException.class, () -> Ulid.fromInstant(Instant.EPOCH, null));
    assertThrows(
        NullPointerException.class,
        () -> Ulid.fromInstant(null, RandomGeneratorFactory.of("L128X256MixRandom").create(1L)));
  }

  @Test
  void fromClockRejectsNulls() {
    assertThrows(NullPointerException.class, () -> Ulid.fromClock(null));
    assertThrows(NullPointerException.class, () -> Ulid.fromClock(Clock.systemUTC(), null));
  }
}
