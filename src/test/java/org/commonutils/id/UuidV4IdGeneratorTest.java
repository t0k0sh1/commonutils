package org.commonutils.id;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;
import java.util.random.RandomGenerator;
import java.util.random.RandomGeneratorFactory;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;

class UuidV4IdGeneratorTest {

  private static final Pattern RFC4122_V4_STRING =
      Pattern.compile("^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$");

  @Test
  void defaultConstructorUsesRandomUuidSemantics() {
    final UuidV4IdGenerator gen = new UuidV4IdGenerator();
    final UUID id = gen.generate();
    assertNotNull(id);
    assertEquals(4, id.version());
    assertEquals(2, id.variant());
    assertTrue(RFC4122_V4_STRING.matcher(id.toString()).matches());
  }

  @Test
  void seededRandomGeneratorIsDeterministicPerAllocationOrder() {
    final long seed = 0xC0FFEE_DEAD_BEEFL;
    final RandomGenerator a = RandomGeneratorFactory.of("L128X256MixRandom").create(seed);
    final RandomGenerator b = RandomGeneratorFactory.of("L128X256MixRandom").create(seed);
    final UUID first = new UuidV4IdGenerator(a).generate();
    final UUID alsoFirst = new UuidV4IdGenerator(b).generate();
    assertEquals(first, alsoFirst);
    assertEquals(4, first.version());
    assertEquals(2, first.variant());
    assertTrue(RFC4122_V4_STRING.matcher(first.toString()).matches());
  }

  @Test
  void secondGenerateAdvancesRandomSequence() {
    final RandomGenerator rng = RandomGeneratorFactory.of("L128X256MixRandom").create(1L);
    final UuidV4IdGenerator gen = new UuidV4IdGenerator(rng);
    final UUID first = gen.generate();
    final UUID second = gen.generate();
    assertEquals(4, second.version());
    assertEquals(2, second.variant());
    assertTrue(RFC4122_V4_STRING.matcher(second.toString()).matches());
    assertTrue(!first.equals(second));
  }

  @Test
  void nullRandomGeneratorRejected() {
    assertThrows(NullPointerException.class, () -> new UuidV4IdGenerator(null));
  }
}
