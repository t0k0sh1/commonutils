package org.commonutils.id;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.random.RandomGenerator;
import java.util.random.RandomGeneratorFactory;
import org.junit.jupiter.api.Test;

class NanoIdGeneratorTest {

  @Test
  void defaultAlphabetConstantMatchesNanoIdSize() {
    assertEquals(64, NanoIdGenerator.DEFAULT_ALPHABET.length());
    assertEquals(64, NanoIdGenerator.DEFAULT_ALPHABET_SIZE);
  }

  @Test
  void defaultConstructorProducesUrlSafeShape() {
    final NanoIdGenerator gen = new NanoIdGenerator();
    final String id = gen.generate();
    assertEquals(NanoIdGenerator.DEFAULT_SIZE, id.length());
    for (int i = 0; i < id.length(); i++) {
      assertTrue(
          NanoIdGenerator.DEFAULT_ALPHABET.indexOf(id.charAt(i)) >= 0,
          "unexpected char at " + i + ": " + id.charAt(i));
    }
  }

  @Test
  void seededRandomGeneratorIsDeterministicPerAllocationOrder() {
    final long seed = 0xC0FFEE_DEAD_BEEFL;
    final RandomGenerator a = RandomGeneratorFactory.of("L128X256MixRandom").create(seed);
    final RandomGenerator b = RandomGeneratorFactory.of("L128X256MixRandom").create(seed);
    final String first = new NanoIdGenerator(a).generate();
    final String alsoFirst = new NanoIdGenerator(b).generate();
    assertEquals(first, alsoFirst);
    assertEquals(NanoIdGenerator.DEFAULT_SIZE, first.length());
  }

  @Test
  void secondGenerateAdvancesRandomSequence() {
    final RandomGenerator rng = RandomGeneratorFactory.of("L128X256MixRandom").create(1L);
    final NanoIdGenerator gen = new NanoIdGenerator(rng);
    final String first = gen.generate();
    final String second = gen.generate();
    assertEquals(NanoIdGenerator.DEFAULT_SIZE, first.length());
    assertEquals(NanoIdGenerator.DEFAULT_SIZE, second.length());
    assertNotEquals(first, second);
  }

  @Test
  void customAlphabetAndSize() {
    final RandomGenerator rng = RandomGeneratorFactory.of("L128X256MixRandom").create(42L);
    final String id = new NanoIdGenerator("abc", 10, rng).generate();
    assertEquals(10, id.length());
    for (int i = 0; i < id.length(); i++) {
      assertTrue("abc".indexOf(id.charAt(i)) >= 0);
    }
  }

  @Test
  void nullRandomGeneratorRejected() {
    assertThrows(NullPointerException.class, () -> new NanoIdGenerator(null));
    assertThrows(NullPointerException.class, () -> new NanoIdGenerator(5, null));
    assertThrows(NullPointerException.class, () -> new NanoIdGenerator("a", 1, null));
  }

  @Test
  void nullAlphabetRejected() {
    assertThrows(NullPointerException.class, () -> new NanoIdGenerator(null, 1));
    assertThrows(
        NullPointerException.class,
        () -> new NanoIdGenerator(null, 1, RandomGenerator.getDefault()));
  }

  @Test
  void nonPositiveSizeRejected() {
    final RandomGenerator rng = RandomGeneratorFactory.of("L128X256MixRandom").create(1L);
    assertThrows(IllegalArgumentException.class, () -> new NanoIdGenerator(0, rng));
    assertThrows(IllegalArgumentException.class, () -> new NanoIdGenerator(-1, rng));
  }

  @Test
  void emptyAlphabetRejected() {
    final RandomGenerator rng = RandomGeneratorFactory.of("L128X256MixRandom").create(1L);
    assertThrows(IllegalArgumentException.class, () -> new NanoIdGenerator("", 1));
    assertThrows(IllegalArgumentException.class, () -> new NanoIdGenerator("", 1, rng));
  }

  @Test
  void duplicateCodePointsRejected() {
    final RandomGenerator rng = RandomGeneratorFactory.of("L128X256MixRandom").create(1L);
    assertThrows(IllegalArgumentException.class, () -> new NanoIdGenerator("aa", 1));
    assertThrows(IllegalArgumentException.class, () -> new NanoIdGenerator("aa", 1, rng));
  }

  @Test
  void loneSurrogateRejected() {
    assertThrows(
        IllegalArgumentException.class,
        () ->
            new NanoIdGenerator(
                "\uD800", 1, RandomGeneratorFactory.of("L128X256MixRandom").create(1L)));
  }

  @Test
  void supplementaryCodePointRejected() {
    assertThrows(
        IllegalArgumentException.class,
        () ->
            new NanoIdGenerator(
                "\uD83D\uDE00", 1, RandomGeneratorFactory.of("L128X256MixRandom").create(1L)));
  }
}
