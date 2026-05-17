package org.commonutils.id;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import java.util.random.RandomGenerator;
import java.util.random.RandomGeneratorFactory;
import org.junit.jupiter.api.Test;

class UuidV7Test {

  private static final UUID RFC9562_APPENDIX_A6 =
      UUID.fromString("017f22e2-79b0-7cc3-98c4-dc0c0c07398f");

  @Test
  void appendixA6ExampleMatchesUuidV7FromParts() {
    final long unixTsMs = 0x017F22E279B0L;
    assertEquals(1645557742000L, unixTsMs);
    final long lsb =
        (0x98L << 56)
            | (0xC4L << 48)
            | (0xDCL << 40)
            | (0x0CL << 32)
            | (0x0CL << 24)
            | (0x07L << 16)
            | (0x39L << 8)
            | 0x8FL;
    final UUID id = UuidV7.uuidV7FromParts(unixTsMs, 0xCC3, lsb);
    assertEquals(RFC9562_APPENDIX_A6, id);
    assertEquals(7, id.version());
    assertEquals(2, id.variant());
  }

  @Test
  void fromInstantUsesVersion7AndRfcVariant() {
    final UUID id = UuidV7.fromInstant(Instant.ofEpochMilli(1_700_000_000_000L), seededRng(42L));
    assertEquals(7, id.version());
    assertEquals(2, id.variant());
    assertEquals(1_700_000_000_000L, id.getMostSignificantBits() >>> 16);
  }

  @Test
  void fromClockDelegatesToInstantSemantics() {
    final Clock clock = Clock.fixed(Instant.ofEpochMilli(1_800_000_000_000L), ZoneOffset.UTC);
    final UUID id = UuidV7.fromClock(clock, seededRng(99L));
    assertEquals(7, id.version());
    assertEquals(1_800_000_000_000L, id.getMostSignificantBits() >>> 16);
  }

  @Test
  void negativeUnixMillisRejected() {
    assertThrows(
        IllegalArgumentException.class,
        () -> UuidV7.fromInstant(Instant.ofEpochMilli(-1L), seededRng(1L)));
  }

  @Test
  void unixMillisAbove48BitsRejected() {
    assertThrows(
        IllegalArgumentException.class,
        () -> UuidV7.fromInstant(Instant.ofEpochMilli(UuidV7.MAX_UNIX_TS_MS + 1L), seededRng(1L)));
  }

  @Test
  void randomInRangeHonoursClosedBoundsWithSeededRng() {
    final Instant min = Instant.ofEpochMilli(100L);
    final Instant max = Instant.ofEpochMilli(103L);
    final RandomGenerator rng = seededRng(12345L);
    final UUID a = UuidV7.fromInstantRandomInRange(min, max, rng);
    final long msA = a.getMostSignificantBits() >>> 16;
    assertTrue(msA >= 100L && msA <= 103L);
    final UUID b = UuidV7.fromInstantRandomInRange(min, max, seededRng(12345L));
    assertEquals(msA, b.getMostSignificantBits() >>> 16);
  }

  @Test
  void orderedBatchWideRangeIsNonDecreasingWithinBounds() {
    final Instant min = Instant.ofEpochMilli(1_000L);
    final Instant max = Instant.ofEpochMilli(1_050L);
    final int count = 8;
    final RandomGenerator rng = seededRng(777L);
    final List<UUID> list = UuidV7.batchFromInstantOrderedInRange(min, max, count, rng);
    assertEquals(count, list.size());
    long prev = Long.MIN_VALUE;
    for (final UUID u : list) {
      assertEquals(7, u.version());
      final long ms = u.getMostSignificantBits() >>> 16;
      assertTrue(ms >= 1_000L && ms <= 1_050L);
      assertTrue(ms >= prev);
      prev = ms;
    }
  }

  @Test
  void orderedBatchNarrowRangeIsNonDecreasing() {
    final Instant t = Instant.ofEpochMilli(500L);
    final int count = 6;
    final List<UUID> list = UuidV7.batchFromInstantOrderedInRange(t, t, count, seededRng(3L));
    long prev = Long.MIN_VALUE;
    for (final UUID u : list) {
      final long ms = u.getMostSignificantBits() >>> 16;
      assertEquals(500L, ms);
      assertTrue(ms >= prev);
      prev = ms;
    }
  }

  @Test
  void orderedBatchCountOneDrawsInsideRange() {
    final Instant min = Instant.ofEpochMilli(10L);
    final Instant max = Instant.ofEpochMilli(20L);
    final RandomGenerator rng = seededRng(555L);
    final UUID u = UuidV7.batchFromInstantOrderedInRange(min, max, 1, rng).get(0);
    final long ms = u.getMostSignificantBits() >>> 16;
    assertTrue(ms >= 10L && ms <= 20L);
  }

  @Test
  void batchFromInstantRepeatsTimestamp() {
    final Instant ins = Instant.ofEpochMilli(42L);
    final List<UUID> list = UuidV7.batchFromInstant(ins, 4, seededRng(9L));
    assertEquals(4, list.size());
    for (final UUID u : list) {
      assertEquals(42L, u.getMostSignificantBits() >>> 16);
    }
    assertNotEquals(list.get(0), list.get(1));
  }

  @Test
  void batchFromClockSamplesClockEachTime() {
    final Clock stepping =
        new Clock() {
          private Instant i = Instant.ofEpochMilli(1L);

          @Override
          public ZoneId getZone() {
            return ZoneOffset.UTC;
          }

          @Override
          public Clock withZone(final ZoneId zone) {
            throw new UnsupportedOperationException();
          }

          @Override
          public Instant instant() {
            final Instant r = i;
            i = i.plusMillis(1L);
            return r;
          }
        };
    final List<UUID> list = UuidV7.batchFromClock(stepping, 3, seededRng(11L));
    assertEquals(1L, list.get(0).getMostSignificantBits() >>> 16);
    assertEquals(2L, list.get(1).getMostSignificantBits() >>> 16);
    assertEquals(3L, list.get(2).getMostSignificantBits() >>> 16);
  }

  @Test
  void minAfterMaxRejectedForRandomInRange() {
    assertThrows(
        IllegalArgumentException.class,
        () ->
            UuidV7.fromInstantRandomInRange(
                Instant.ofEpochMilli(10L), Instant.ofEpochMilli(9L), seededRng(1L)));
  }

  @Test
  void nullInstantRejected() {
    assertThrows(NullPointerException.class, () -> UuidV7.fromInstant(null, seededRng(1L)));
  }

  @Test
  void nonPositiveCountRejected() {
    assertThrows(
        IllegalArgumentException.class,
        () -> UuidV7.batchFromInstant(Instant.EPOCH, 0, seededRng(1L)));
  }

  private static RandomGenerator seededRng(final long seed) {
    return RandomGeneratorFactory.of("L128X256MixRandom").create(seed);
  }
}
