package org.commonutils.id;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.random.RandomGenerator;
import java.util.random.RandomGeneratorFactory;
import org.junit.jupiter.api.Test;

class MutableNanoIdGeneratorTest {

  @Test
  void defaultConstructorProducesUrlSafeShape() {
    final MutableNanoIdGenerator gen = new MutableNanoIdGenerator();
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
    final String first = new MutableNanoIdGenerator(a).generate();
    final String alsoFirst = new MutableNanoIdGenerator(b).generate();
    assertEquals(first, alsoFirst);
    assertEquals(NanoIdGenerator.DEFAULT_SIZE, first.length());
  }

  @Test
  void configureUpdatesAlphabetAndSize() {
    final RandomGenerator rng = RandomGeneratorFactory.of("L128X256MixRandom").create(99L);
    final MutableNanoIdGenerator gen = new MutableNanoIdGenerator(rng);
    gen.configure("abc", 7);
    final String id = gen.generate();
    assertEquals(7, id.length());
    for (int i = 0; i < id.length(); i++) {
      assertTrue("abc".indexOf(id.charAt(i)) >= 0);
    }
  }

  @Test
  void setAlphabetPreservesLatestSize() {
    final RandomGenerator rng = RandomGeneratorFactory.of("L128X256MixRandom").create(7L);
    final MutableNanoIdGenerator gen = new MutableNanoIdGenerator(5, rng);
    gen.setAlphabet("xyz");
    final String id = gen.generate();
    assertEquals(5, id.length());
    for (int i = 0; i < id.length(); i++) {
      assertTrue("xyz".indexOf(id.charAt(i)) >= 0);
    }
  }

  @Test
  void setSizePreservesAlphabet() {
    final RandomGenerator rng = RandomGeneratorFactory.of("L128X256MixRandom").create(11L);
    final MutableNanoIdGenerator gen = new MutableNanoIdGenerator("pq", 3, rng);
    gen.setSize(9);
    final String id = gen.generate();
    assertEquals(9, id.length());
    for (int i = 0; i < id.length(); i++) {
      assertTrue("pq".indexOf(id.charAt(i)) >= 0);
    }
  }

  @Test
  void successiveGeneratesAdvanceRandomSequence() {
    final RandomGenerator rng = RandomGeneratorFactory.of("L128X256MixRandom").create(3L);
    final MutableNanoIdGenerator gen = new MutableNanoIdGenerator(rng);
    final String first = gen.generate();
    final String second = gen.generate();
    assertEquals(NanoIdGenerator.DEFAULT_SIZE, first.length());
    assertEquals(NanoIdGenerator.DEFAULT_SIZE, second.length());
    assertNotEquals(first, second);
  }

  @Test
  void nonCryptographicDefaultsMatchNanoDefaults() {
    final MutableNanoIdGenerator gen = MutableNanoIdGenerator.nonCryptographic();
    final String id = gen.generate();
    assertEquals(NanoIdGenerator.DEFAULT_SIZE, id.length());
    assertNotEquals(gen.generate(), id);
  }

  @Test
  void nonCryptographicFactoryWithAlphabetAndSize() {
    final MutableNanoIdGenerator gen = MutableNanoIdGenerator.nonCryptographic("01", 16);
    final String id = gen.generate();
    assertEquals(16, id.length());
    for (int i = 0; i < id.length(); i++) {
      assertTrue(id.charAt(i) == '0' || id.charAt(i) == '1');
    }
  }

  @Test
  void nonCryptographicWithSizeProducesDistinctSuccessiveIds() {
    final MutableNanoIdGenerator gen = MutableNanoIdGenerator.nonCryptographic(11);
    assertEquals(11, gen.generate().length());
    assertNotEquals(gen.generate(), gen.generate());
  }

  @Test
  void nonCryptographicFactoryRejectsInvalidAlphabets() {
    assertThrows(
        IllegalArgumentException.class, () -> MutableNanoIdGenerator.nonCryptographic("", 2));
    assertThrows(
        IllegalArgumentException.class, () -> MutableNanoIdGenerator.nonCryptographic("aa", 2));
  }

  @Test
  void concurrentConfigureAndGenerateProduceSnapshotCoherentIds() throws InterruptedException {
    final RandomGenerator rng = RandomGeneratorFactory.of("SecureRandom").create();
    final MutableNanoIdGenerator gen = new MutableNanoIdGenerator(rng);
    gen.configure("01", 10);

    final String[] alphabets = {"01", "ab", "XY"};
    final int[] sizes = {10, 12, 15};
    final int updaterThreads = 2;
    final int generatorThreads = 4;
    final int generatesPerThread = 2500;

    final ExecutorService pool = Executors.newFixedThreadPool(updaterThreads + generatorThreads);
    try {
      final AtomicBoolean coherent = new AtomicBoolean(true);
      for (int u = 0; u < updaterThreads; u++) {
        pool.submit(
            () -> {
              for (int i = 0; i < 8000 && coherent.get(); i++) {
                final int idx = ThreadLocalRandom.current().nextInt(alphabets.length);
                gen.configure(alphabets[idx], sizes[idx]);
              }
            });
      }
      for (int g = 0; g < generatorThreads; g++) {
        pool.submit(
            () -> {
              for (int i = 0; i < generatesPerThread && coherent.get(); i++) {
                final String id = gen.generate();
                if (!matchesOneAllowedSnapshot(id, alphabets, sizes)) {
                  coherent.set(false);
                  return;
                }
              }
            });
      }
      pool.shutdown();
      assertTrue(pool.awaitTermination(120, TimeUnit.SECONDS));
      assertTrue(coherent.get(), "generated id violated snapshot coherence");
    } finally {
      pool.shutdownNow();
    }
  }

  @Test
  void nullRandomGeneratorRejected() {
    assertThrows(NullPointerException.class, () -> new MutableNanoIdGenerator(null));
    assertThrows(NullPointerException.class, () -> new MutableNanoIdGenerator(5, null));
    assertThrows(NullPointerException.class, () -> new MutableNanoIdGenerator("a", 1, null));
  }

  @Test
  void nullAlphabetRejectedOnConstructionAndConfigure() {
    assertThrows(NullPointerException.class, () -> new MutableNanoIdGenerator(null, 1));
    assertThrows(
        NullPointerException.class,
        () -> new MutableNanoIdGenerator(null, 1, RandomGenerator.getDefault()));
    final MutableNanoIdGenerator gen = new MutableNanoIdGenerator();
    assertThrows(NullPointerException.class, () -> gen.configure(null, 1));
    assertThrows(NullPointerException.class, () -> gen.setAlphabet(null));
  }

  @Test
  void nonPositiveSizeRejectedOnConstructionAndMutators() {
    final RandomGenerator rng = RandomGeneratorFactory.of("L128X256MixRandom").create(1L);
    assertThrows(IllegalArgumentException.class, () -> new MutableNanoIdGenerator(0, rng));
    assertThrows(IllegalArgumentException.class, () -> new MutableNanoIdGenerator(-1, rng));

    final MutableNanoIdGenerator gen = new MutableNanoIdGenerator(rng);
    assertThrows(IllegalArgumentException.class, () -> gen.configure("ab", 0));
    assertThrows(IllegalArgumentException.class, () -> gen.setSize(0));
    assertThrows(IllegalArgumentException.class, () -> MutableNanoIdGenerator.nonCryptographic(0));
  }

  @Test
  void emptyAlphabetRejected() {
    final RandomGenerator rng = RandomGeneratorFactory.of("L128X256MixRandom").create(1L);
    assertThrows(IllegalArgumentException.class, () -> new MutableNanoIdGenerator("", 1));
    assertThrows(IllegalArgumentException.class, () -> new MutableNanoIdGenerator("", 1, rng));
    final MutableNanoIdGenerator gen = new MutableNanoIdGenerator(rng);
    assertThrows(IllegalArgumentException.class, () -> gen.configure("", 1));
  }

  @Test
  void duplicateCodePointsRejected() {
    final RandomGenerator rng = RandomGeneratorFactory.of("L128X256MixRandom").create(1L);
    assertThrows(IllegalArgumentException.class, () -> new MutableNanoIdGenerator("aa", 1));
    assertThrows(IllegalArgumentException.class, () -> new MutableNanoIdGenerator("aa", 1, rng));
    final MutableNanoIdGenerator gen = new MutableNanoIdGenerator(rng);
    assertThrows(IllegalArgumentException.class, () -> gen.setAlphabet("aa"));
    assertThrows(IllegalArgumentException.class, () -> gen.configure("aa", 2));
  }

  private static boolean matchesOneAllowedSnapshot(
      final String id, final String[] alphabets, final int[] sizes) {
    for (int k = 0; k < alphabets.length; k++) {
      if (matchesAlphabetAndLength(id, alphabets[k], sizes[k])) {
        return true;
      }
    }
    return false;
  }

  private static boolean matchesAlphabetAndLength(
      final String id, final String alphabet, final int size) {
    if (id.length() != size) {
      return false;
    }
    for (int i = 0; i < id.length(); i++) {
      if (alphabet.indexOf(id.charAt(i)) < 0) {
        return false;
      }
    }
    return true;
  }
}
