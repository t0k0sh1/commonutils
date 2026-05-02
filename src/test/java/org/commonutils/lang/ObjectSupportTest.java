package org.commonutils.lang;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Comparator;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import org.junit.jupiter.api.Test;

class ObjectSupportTest {

  @Test
  void equalsMatchesNullAndValueSemantics() {
    assertTrue(ObjectSupport.equals(null, null));
    assertFalse(ObjectSupport.equals(null, "x"));
    assertFalse(ObjectSupport.equals("x", null));
    assertTrue(ObjectSupport.equals("x", "x"));
    assertFalse(ObjectSupport.equals("x", "y"));
    assertFalse(ObjectSupport.equals(Integer.valueOf(1), Long.valueOf(1)));
    assertFalse(ObjectSupport.equals("ab", "a"));
  }

  @Test
  void deepEqualsHandlesArrays() {
    assertTrue(ObjectSupport.deepEquals(new int[] {1, 2}, new int[] {1, 2}));
    assertFalse(ObjectSupport.deepEquals(new int[] {1}, new int[] {1, 2}));
    assertTrue(ObjectSupport.deepEquals(null, null));
    assertFalse(ObjectSupport.deepEquals(null, new int[] {1}));
    assertFalse(ObjectSupport.deepEquals(new int[] {1}, null));
    assertFalse(ObjectSupport.deepEquals(new int[] {1}, new int[] {2}));
  }

  @Test
  void hashCodeAndHashDelegate() {
    assertEquals(0, ObjectSupport.hashCode(null));
    assertEquals("a".hashCode(), ObjectSupport.hashCode("a"));
    assertEquals(java.util.Objects.hash("a", "b"), ObjectSupport.hash("a", "b"));
    assertEquals(java.util.Objects.hash(), ObjectSupport.hash());
    assertEquals(java.util.Objects.hash((Object) null), ObjectSupport.hash((Object) null));
  }

  @Test
  void toStringVariantsDelegate() {
    assertEquals("null", ObjectSupport.toString(null));
    assertEquals("fallback", ObjectSupport.toString(null, "fallback"));
    assertEquals("hi", ObjectSupport.toString("hi", "fallback"));
    assertNull(ObjectSupport.toString(null, null));
    final AtomicInteger lazyCalls = new AtomicInteger();
    assertEquals(
        "present",
        ObjectSupport.toStringElseGet(
            "present",
            () -> {
              lazyCalls.incrementAndGet();
              return "lazy";
            }));
    assertEquals(0, lazyCalls.get());
    assertEquals("lazy", ObjectSupport.toStringElseGet(null, () -> "lazy"));
    lazyCalls.set(0);
    assertNull(
        ObjectSupport.toStringElseGet(
            null,
            () -> {
              lazyCalls.incrementAndGet();
              return null;
            }));
    assertEquals(1, lazyCalls.get());
    assertThrows(NullPointerException.class, () -> ObjectSupport.toStringElseGet(null, null));
    final Object o = new Object();
    assertTrue(ObjectSupport.toIdentityString(o).contains(o.getClass().getName()));
  }

  @Test
  void firstNonNullAndLastNonNull() {
    assertNull(ObjectSupport.firstNonNull());
    assertNull(ObjectSupport.firstNonNull((Object[]) null));
    assertEquals("a", ObjectSupport.firstNonNull(null, "a", "b"));
    assertEquals("a", ObjectSupport.firstNonNull("a", "b"));
    assertEquals("b", ObjectSupport.lastNonNull(null, "a", "b"));
    assertEquals("c", ObjectSupport.lastNonNull(null, "a", null, "c"));
    assertNull(ObjectSupport.lastNonNull((Object[]) null));
    assertNull(ObjectSupport.lastNonNull(null, null, null));
    assertNull(ObjectSupport.firstNonNull(new String[0]));
    assertNull(ObjectSupport.lastNonNull(new String[0]));
  }

  @Test
  void requireNonNullChecksMultipleRefs() {
    assertDoesNotThrow(() -> ObjectSupport.requireNonNull("a", "b", "c"));
    assertThrows(NullPointerException.class, () -> ObjectSupport.requireNonNull(null, "b"));
    assertThrows(
        NullPointerException.class, () -> ObjectSupport.requireNonNull("a", new Object[] {null}));
    assertThrows(
        NullPointerException.class, () -> ObjectSupport.requireNonNull("ok", (Object[]) null));
  }

  @Test
  void compareUsesComparator() {
    assertEquals(0, ObjectSupport.compare(1, 1, Comparator.naturalOrder()));
    assertTrue(ObjectSupport.compare(1, 2, Comparator.naturalOrder()) < 0);
    assertThrows(
        NullPointerException.class,
        () -> ObjectSupport.compare(null, 1, Comparator.naturalOrder()));
    assertThrows(
        NullPointerException.class,
        () -> ObjectSupport.compare(1, null, Comparator.naturalOrder()));
    assertEquals(
        0, ObjectSupport.compare(null, null, Comparator.nullsFirst(Comparator.naturalOrder())));
  }

  @Test
  void compareRequiresNonNullComparator() {
    assertThrows(NullPointerException.class, () -> ObjectSupport.compare(1, 2, null));
  }

  @Test
  void isNullAndNonNull() {
    assertTrue(ObjectSupport.isNull(null));
    assertFalse(ObjectSupport.isNull(""));
    assertFalse(ObjectSupport.nonNull(null));
    assertTrue(ObjectSupport.nonNull(""));
  }

  @Test
  void requireNonNullReturnsValueOrThrows() {
    assertEquals("ok", ObjectSupport.requireNonNull("ok"));
    assertThrows(NullPointerException.class, () -> ObjectSupport.requireNonNull(null));
    final NullPointerException withMsg =
        assertThrows(
            NullPointerException.class, () -> ObjectSupport.requireNonNull(null, "missing"));
    assertTrue(withMsg.getMessage().contains("missing"));
    assertThrows(
        NullPointerException.class, () -> ObjectSupport.requireNonNull(null, () -> "lazy"));
  }

  @Test
  void requireNonNullElseAndElseGet() {
    assertEquals("a", ObjectSupport.requireNonNullElse("a", "b"));
    assertEquals("b", ObjectSupport.requireNonNullElse(null, "b"));
    assertThrows(NullPointerException.class, () -> ObjectSupport.requireNonNullElse(null, null));
    assertEquals("x", ObjectSupport.requireNonNullElseGet(null, () -> "x"));
    assertThrows(
        NullPointerException.class, () -> ObjectSupport.requireNonNullElseGet(null, () -> null));
    assertEquals("keep", ObjectSupport.requireNonNullElseGet("keep", () -> "ignored"));
    assertThrows(
        NullPointerException.class,
        () -> ObjectSupport.requireNonNullElseGet("ok", (Supplier<? extends String>) null));
  }

  @Test
  void checkIndexMethodsAcceptValidRanges() {
    assertEquals(2, ObjectSupport.checkIndex(2, 10));
    assertEquals(1, ObjectSupport.checkFromToIndex(1, 4, 10));
    assertEquals(3, ObjectSupport.checkFromIndexSize(3, 2, 10));
    assertEquals(2L, ObjectSupport.checkIndex(2L, 10L));
    assertEquals(1L, ObjectSupport.checkFromToIndex(1L, 4L, 10L));
    assertEquals(3L, ObjectSupport.checkFromIndexSize(3L, 2L, 10L));
    assertEquals(0, ObjectSupport.checkFromToIndex(0, 0, 5));
    assertEquals(0, ObjectSupport.checkIndex(0, 1));
    assertEquals(0, ObjectSupport.checkFromToIndex(0, 5, 5));
    assertEquals(5, ObjectSupport.checkFromIndexSize(5, 0, 5));
    assertEquals(0L, ObjectSupport.checkFromToIndex(0L, 0L, 5L));
    assertEquals(Long.MAX_VALUE - 1, ObjectSupport.checkIndex(Long.MAX_VALUE - 1, Long.MAX_VALUE));
  }

  @Test
  void checkIndexMethodsRejectInvalidRanges() {
    assertThrows(IndexOutOfBoundsException.class, () -> ObjectSupport.checkIndex(-1, 5));
    assertThrows(IndexOutOfBoundsException.class, () -> ObjectSupport.checkFromToIndex(3, 2, 10));
    assertThrows(IndexOutOfBoundsException.class, () -> ObjectSupport.checkFromIndexSize(9, 5, 10));
    assertThrows(IndexOutOfBoundsException.class, () -> ObjectSupport.checkIndex(5, 5));
    assertThrows(IndexOutOfBoundsException.class, () -> ObjectSupport.checkIndex(10, 10));
    assertThrows(IndexOutOfBoundsException.class, () -> ObjectSupport.checkFromToIndex(0, 6, 5));
    assertThrows(IndexOutOfBoundsException.class, () -> ObjectSupport.checkFromIndexSize(6, 5, 10));
    assertThrows(
        IndexOutOfBoundsException.class, () -> ObjectSupport.checkFromIndexSize(-1, 1, 10));
  }

  @Test
  void checkIndexMethodsRejectNegativeLengthOrSize() {
    assertThrows(IllegalArgumentException.class, () -> ObjectSupport.checkIndex(0, -1));
    assertThrows(IllegalArgumentException.class, () -> ObjectSupport.checkFromToIndex(0, 1, -1));
    assertThrows(IllegalArgumentException.class, () -> ObjectSupport.checkFromIndexSize(0, -1, 10));
    assertThrows(IllegalArgumentException.class, () -> ObjectSupport.checkFromIndexSize(0, 2, -1));
    assertThrows(IllegalArgumentException.class, () -> ObjectSupport.checkIndex(0L, -1L));
    assertThrows(
        IllegalArgumentException.class, () -> ObjectSupport.checkFromIndexSize(0L, -1L, 10L));
    final IllegalArgumentException badLength =
        assertThrows(IllegalArgumentException.class, () -> ObjectSupport.checkIndex(0, -1));
    assertTrue(badLength.getMessage().contains("length"));
    final IllegalArgumentException badSize =
        assertThrows(
            IllegalArgumentException.class, () -> ObjectSupport.checkFromIndexSize(0, -3, 10));
    assertTrue(badSize.getMessage().contains("size"));
  }

  @Test
  void toIdentityStringRequiresNonNull() {
    assertThrows(NullPointerException.class, () -> ObjectSupport.toIdentityString(null));
  }

  @Test
  void requireNonNullReturnsSameInstance() {
    final String s = "ref";
    assertSame(s, ObjectSupport.requireNonNull(s));
    assertSame(s, ObjectSupport.requireNonNull(s, "nope"));
    assertSame(s, ObjectSupport.requireNonNull(s, () -> "nope"));
  }

  @Test
  void requireNonNullElseGetDoesNotInvokeSupplierWhenPresent() {
    assertEquals(
        "a",
        ObjectSupport.requireNonNullElseGet(
            "a",
            () -> {
              throw new AssertionError("supplier should not run");
            }));
  }

  @Test
  void messageSupplierNotInvokedWhenValueNonNull() {
    assertDoesNotThrow(
        () ->
            ObjectSupport.requireNonNull(
                "ok",
                () -> {
                  throw new AssertionError("supplier should not run");
                }));
  }

  @Test
  void requireNonNullSupplierOverloadRequiresNonNullSupplier() {
    assertThrows(
        NullPointerException.class,
        () -> ObjectSupport.requireNonNull("ok", (java.util.function.Supplier<String>) null));
  }

  @Test
  void requireNonNullSingleArgOverloadIsNotVarargsOverload() {
    assertEquals("a", ObjectSupport.requireNonNull("a"));
    assertThrows(
        NullPointerException.class, () -> ObjectSupport.requireNonNull(null, (String) null));
    assertDoesNotThrow(() -> ObjectSupport.requireNonNull("a", (String) null));
  }

  @Test
  void checkFromIndexSizeLongRejectsNegativeLength() {
    assertThrows(
        IllegalArgumentException.class, () -> ObjectSupport.checkFromIndexSize(0L, 1L, -1L));
  }

  @Test
  void checkIndexLongRejectsOutOfRange() {
    assertThrows(IndexOutOfBoundsException.class, () -> ObjectSupport.checkIndex(-1L, 5L));
    assertThrows(IndexOutOfBoundsException.class, () -> ObjectSupport.checkIndex(5L, 5L));
  }

  @Test
  void checkFromToIndexLongRejectsInvertedRange() {
    assertThrows(
        IndexOutOfBoundsException.class, () -> ObjectSupport.checkFromToIndex(4L, 3L, 10L));
  }

  @Test
  void toIdentityStringIncludesIdentityHashWhenDistinct() {
    final Object a = new Object();
    final Object b = new Object();
    assertNotEquals(ObjectSupport.toIdentityString(a), ObjectSupport.toIdentityString(b));
  }
}
