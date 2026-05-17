package org.commonutils.id;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.HashSet;
import java.util.Set;
import java.util.random.RandomGenerator;
import java.util.random.RandomGeneratorFactory;
import org.junit.jupiter.api.Test;

class Cuid2IdGeneratorTest {

  private static final class ScriptedRandom implements RandomGenerator {
    private final int[] values;
    private int index;

    ScriptedRandom(final int[] values) {
      this.values = values;
    }

    @Override
    public int nextInt(final int bound) {
      return values[index++];
    }

    @Override
    public long nextLong() {
      return 0L;
    }
  }

  @Test
  void goldenSequenceWithFixedClockCounterFingerprintAndRng() {
    final int len = Cuid2.DEFAULT_LENGTH;
    final int[] zeros = new int[(len + 1) * 2];
    final RandomGenerator rng = new ScriptedRandom(zeros);
    final Clock clock = Clock.fixed(Instant.EPOCH, ZoneOffset.UTC);
    final String fp = "b".repeat(Cuid2.FINGERPRINT_LENGTH);
    final Cuid2IdGenerator gen = new Cuid2IdGenerator(len, rng, clock, fp, 0L);
    assertEquals("azp6e0zyb6zay5xmvpryutuw", gen.generate());
    assertEquals("ajwvnos6t5dner4eo71gvxf4", gen.generate());
  }

  @Test
  void shapeAndLengthMatchConfiguration() {
    final Cuid2IdGenerator gen =
        new Cuid2IdGenerator(
            8,
            RandomGeneratorFactory.of("L128X256MixRandom").create(99L),
            Clock.fixed(Instant.ofEpochMilli(4096L), ZoneOffset.UTC),
            "fpfpfpfpfpfpfpfpfpfpfpfpfpfpfpfp",
            0L);
    final String id = gen.generate();
    assertEquals(8, id.length());
    assertTrue(Cuid2.isValid(id));
    assertTrue(id.charAt(0) >= 'a' && id.charAt(0) <= 'z');
  }

  @Test
  void defaultLengthAndCustomLengthConstructors() {
    assertEquals(Cuid2.DEFAULT_LENGTH, new Cuid2IdGenerator().generate().length());
    assertEquals(10, new Cuid2IdGenerator(10).generate().length());
  }

  @Test
  void successiveGeneratesDiffer() {
    final Cuid2IdGenerator gen =
        new Cuid2IdGenerator(
            Cuid2.DEFAULT_LENGTH,
            RandomGeneratorFactory.of("L128X256MixRandom").create(7L),
            Clock.systemUTC());
    final String a = gen.generate();
    final String b = gen.generate();
    assertNotEquals(a, b);
  }

  @Test
  void lightLoopDistinct() {
    final Cuid2IdGenerator gen =
        new Cuid2IdGenerator(
            Cuid2.DEFAULT_LENGTH,
            RandomGeneratorFactory.of("L128X256MixRandom").create(200L),
            Clock.systemUTC());
    final Set<String> seen = new HashSet<>();
    for (int i = 0; i < 200; i++) {
      assertTrue(seen.add(gen.generate()));
    }
  }

  @Test
  void lengthOutOfRangeRejected() {
    assertThrows(IllegalArgumentException.class, () -> new Cuid2IdGenerator(1));
    assertThrows(IllegalArgumentException.class, () -> new Cuid2IdGenerator(33));
    assertThrows(IllegalArgumentException.class, () -> Cuid2IdGenerator.nonCryptographic(1));
  }

  @Test
  void emptyCustomFingerprintRejected() {
    assertThrows(
        IllegalArgumentException.class,
        () ->
            new Cuid2IdGenerator(
                Cuid2.DEFAULT_LENGTH,
                RandomGeneratorFactory.of("L128X256MixRandom").create(1L),
                Clock.systemUTC(),
                ""));
  }

  @Test
  void nullArgumentsRejected() {
    assertThrows(NullPointerException.class, () -> new Cuid2IdGenerator(12, null));
    assertThrows(
        NullPointerException.class,
        () ->
            new Cuid2IdGenerator(
                12,
                RandomGeneratorFactory.of("L128X256MixRandom").create(1L),
                null,
                "abababababababababababababababab",
                0L));
    assertThrows(
        NullPointerException.class,
        () ->
            new Cuid2IdGenerator(
                12,
                RandomGeneratorFactory.of("L128X256MixRandom").create(1L),
                Clock.systemUTC(),
                (String) null,
                0L));
  }

  @Test
  void isValidRejectsNull() {
    assertThrows(NullPointerException.class, () -> Cuid2.isValid(null));
  }

  @Test
  void isValidAcceptsAndRejects() {
    assertTrue(Cuid2.isValid("ab"));
    assertTrue(Cuid2.isValid("azp6e0zyb6zay5xmvpryutuw"));
    assertFalse(Cuid2.isValid("Azp6e0zyb6zay5xmvpryutuw"));
    assertFalse(Cuid2.isValid("9zp6e0zyb6zay5xmvpryutuw"));
    assertFalse(Cuid2.isValid("a"));
    assertFalse(Cuid2.isValid("a" + "x".repeat(32)));
  }
}
