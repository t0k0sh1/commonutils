package org.commonutils.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

class MapSupportTest {
  @Test
  void isEmptyAndIsNotEmptyReflectMapContents() {
    assertTrue(MapSupport.isEmpty(null));
    assertTrue(MapSupport.isEmpty(Map.of()));
    assertFalse(MapSupport.isEmpty(Map.of("a", 1)));

    assertFalse(MapSupport.isNotEmpty(null));
    assertFalse(MapSupport.isNotEmpty(Map.of()));
    assertTrue(MapSupport.isNotEmpty(Map.of("a", 1)));
  }

  @Test
  void defaultIfNullReturnsFallbackOnlyForNullMap() {
    final Map<String, Integer> values = Map.of("a", 1);
    final Map<String, Integer> fallback = Map.of("b", 2);

    assertSame(fallback, MapSupport.defaultIfNull(null, fallback));
    assertSame(values, MapSupport.defaultIfNull(values, fallback));
  }

  @Test
  void getStringReturnsStringValueOrDefault() {
    final Map<String, Object> values = new LinkedHashMap<>();
    values.put("name", "alice");
    values.put("count", 3);
    values.put("nullValue", null);

    assertEquals("alice", MapSupport.getString(values, "name"));
    assertEquals("3", MapSupport.getString(values, "count"));
    assertNull(MapSupport.getString(values, "missing"));
    assertEquals("fallback", MapSupport.getString(values, "missing", "fallback"));
    assertEquals("fallback", MapSupport.getString(values, "nullValue", "fallback"));
  }

  @Test
  void getIntReturnsNumericValueOrDefault() {
    final Map<String, Object> values = new LinkedHashMap<>();
    values.put("number", 12);
    values.put("textNumber", " 34 ");
    values.put("invalid", "x");
    values.put("nullValue", null);

    assertEquals(12, MapSupport.getInt(values, "number"));
    assertEquals(34, MapSupport.getInt(values, "textNumber"));
    assertEquals(0, MapSupport.getInt(values, "missing"));
    assertEquals(7, MapSupport.getInt(values, "invalid", 7));
    assertEquals(7, MapSupport.getInt(values, "nullValue", 7));
  }

  @Test
  void getLongReturnsNumericValueOrDefault() {
    final Map<String, Object> values = new LinkedHashMap<>();
    values.put("number", 12L);
    values.put("textNumber", " 34 ");
    values.put("invalid", "x");

    assertEquals(12L, MapSupport.getLong(values, "number"));
    assertEquals(34L, MapSupport.getLong(values, "textNumber"));
    assertEquals(0L, MapSupport.getLong(values, "missing"));
    assertEquals(7L, MapSupport.getLong(values, "invalid", 7L));
  }

  @Test
  void getBooleanUsesJdkParseBooleanAndDefaultForNull() {
    final Map<String, Object> values = new LinkedHashMap<>();
    values.put("boolTrue", true);
    values.put("boolFalse", false);
    values.put("textTrue", "true");
    values.put("textTrueIgnoreCase", "TRUE");
    values.put("textNotParseableTrue", " yes ");
    values.put("textFalse", "0");
    values.put("textOn", "on");
    values.put("invalid", "maybe");
    values.put("nullValue", null);

    assertTrue(MapSupport.getBoolean(values, "boolTrue"));
    assertFalse(MapSupport.getBoolean(values, "boolFalse", true));
    assertTrue(MapSupport.getBoolean(values, "textTrue"));
    assertTrue(MapSupport.getBoolean(values, "textTrueIgnoreCase"));
    assertFalse(MapSupport.getBoolean(values, "textNotParseableTrue"));
    assertFalse(MapSupport.getBoolean(values, "textFalse", true));
    assertFalse(MapSupport.getBoolean(values, "textOn", true));
    assertFalse(MapSupport.getBoolean(values, "missing"));
    assertFalse(MapSupport.getBoolean(values, "invalid", true));
    assertTrue(MapSupport.getBoolean(values, "nullValue", true));
  }

  @Test
  void defaultIfNullThrowsWhenBothMapsAreNull() {
    assertThrows(NullPointerException.class, () -> MapSupport.defaultIfNull(null, null));
  }

  @Test
  void getIntTruncatesDoubleViaNumberPath() {
    final Map<String, Object> values = new LinkedHashMap<>();
    values.put("fraction", 1.9);
    assertEquals(1, MapSupport.getInt(values, "fraction"));
  }

  @Test
  void getLongUsesLongValueForIntegralTypes() {
    final Map<String, Object> values = new LinkedHashMap<>();
    values.put("big", new BigInteger("9223372036854775807"));
    assertEquals(Long.MAX_VALUE, MapSupport.getLong(values, "big"));
  }

  @Test
  void getIntFromBigDecimalUsesIntValue() {
    final Map<String, Object> values = new LinkedHashMap<>();
    values.put("bd", new BigDecimal("42.7"));
    assertEquals(42, MapSupport.getInt(values, "bd"));
  }

  @Test
  void getStringReadsNullKeyWhenMapSupportsNullKeys() {
    final Map<Object, String> withNullKey = new HashMap<>();
    withNullKey.put(null, "nil");
    assertEquals("nil", MapSupport.getString(withNullKey, null));
    assertEquals("nil", MapSupport.getString(withNullKey, null, "d"));

    final Map<Object, String> withoutNullKey = new HashMap<>();
    withoutNullKey.put("k", "v");
    assertEquals("fallback", MapSupport.getString(withoutNullKey, null, "fallback"));
  }

  @Test
  void getBooleanTreatsEmptyStringAsFalse() {
    final Map<String, Object> values = new LinkedHashMap<>();
    values.put("emptyText", "");
    assertFalse(MapSupport.getBoolean(values, "emptyText", true));
  }
}
