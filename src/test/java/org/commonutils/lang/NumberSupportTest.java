package org.commonutils.lang;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class NumberSupportTest {
  @Test
  void isDigitsReturnsTrueOnlyForDigitOnlyValues() {
    assertFalse(NumberSupport.isDigits(null));
    assertFalse(NumberSupport.isDigits(""));
    assertTrue(NumberSupport.isDigits("123456"));
    assertTrue(NumberSupport.isDigits("0"));
    assertTrue(NumberSupport.isDigits("007"));
    assertTrue(NumberSupport.isDigits("\uFF11"));
    assertFalse(NumberSupport.isDigits("12a3"));
    assertFalse(NumberSupport.isDigits("-123"));
    assertFalse(NumberSupport.isDigits("12 3"));
    assertFalse(NumberSupport.isDigits(" "));
    assertFalse(NumberSupport.isDigits("12\u30003"));
    assertTrue(NumberSupport.isDigits(new StringBuilder("42")));
  }

  @Test
  void isParsableAcceptsStandardNumericStrings() {
    assertFalse(NumberSupport.isParsable(null));
    assertFalse(NumberSupport.isParsable(""));
    assertFalse(NumberSupport.isParsable("   "));
    assertFalse(NumberSupport.isParsable("\t"));
    assertTrue(NumberSupport.isParsable("123"));
    assertTrue(NumberSupport.isParsable(" 123.45 "));
    assertTrue(NumberSupport.isParsable("-42"));
    assertTrue(NumberSupport.isParsable("+99"));
    assertFalse(NumberSupport.isParsable("12a"));
    assertFalse(NumberSupport.isParsable("\u3000"));
  }

  @Test
  void isParsableMatchesDoubleParseDoubleSpecialForms() {
    assertTrue(NumberSupport.isParsable("1e2"));
    assertTrue(NumberSupport.isParsable("NaN"));
    assertTrue(NumberSupport.isParsable("Infinity"));
    assertTrue(NumberSupport.isParsable("-Infinity"));
    assertTrue(NumberSupport.isParsable("0x1.0p0"));
  }

  @Test
  void toIntReturnsParsedValueOrDefault() {
    assertEquals(123, NumberSupport.toInt("123"));
    assertEquals(123, NumberSupport.toInt(" 123 "));
    assertEquals(123, NumberSupport.toInt("+123"));
    assertEquals(0, NumberSupport.toInt(null));
    assertEquals(7, NumberSupport.toInt("x", 7));
    assertEquals(7, NumberSupport.toInt("", 7));
    assertEquals(7, NumberSupport.toInt("   ", 7));
    assertEquals(7, NumberSupport.toInt("2147483648", 7));
    assertEquals(-1, NumberSupport.toInt("-1"));
    assertEquals(Integer.MAX_VALUE, NumberSupport.toInt(String.valueOf(Integer.MAX_VALUE)));
    assertEquals(Integer.MIN_VALUE, NumberSupport.toInt(String.valueOf(Integer.MIN_VALUE)));
  }

  @Test
  void parseIntReturnsParsedValueOrThrows() {
    assertEquals(123, NumberSupport.parseInt("123"));
    assertEquals(123, NumberSupport.parseInt(" 123 "));
    assertEquals(123, NumberSupport.parseInt("+123"));
    final NumberFormatException nfeNull =
        assertThrows(NumberFormatException.class, () -> NumberSupport.parseInt(null));
    assertTrue(nfeNull.getMessage().contains("null") || nfeNull.getMessage().contains("blank"));
    assertThrows(NumberFormatException.class, () -> NumberSupport.parseInt(" "));
    assertThrows(NumberFormatException.class, () -> NumberSupport.parseInt("x"));
    assertThrows(NumberFormatException.class, () -> NumberSupport.parseInt("2147483648"));
    assertThrows(NumberFormatException.class, () -> NumberSupport.parseInt("1.0"));
  }

  @Test
  void toLongReturnsParsedValueOrDefault() {
    assertEquals(123L, NumberSupport.toLong("123"));
    assertEquals(123L, NumberSupport.toLong(" 123 "));
    assertEquals(0L, NumberSupport.toLong(null));
    assertEquals(7L, NumberSupport.toLong("x", 7L));
    assertEquals(7L, NumberSupport.toLong("", 7L));
    assertEquals(7L, NumberSupport.toLong("9223372036854775808", 7L));
    assertEquals(Long.MIN_VALUE, NumberSupport.toLong(String.valueOf(Long.MIN_VALUE)));
    assertEquals(Long.MAX_VALUE, NumberSupport.toLong(String.valueOf(Long.MAX_VALUE)));
  }

  @Test
  void parseLongReturnsParsedValueOrThrows() {
    assertEquals(123L, NumberSupport.parseLong("123"));
    assertEquals(123L, NumberSupport.parseLong(" 123 "));
    assertThrows(NumberFormatException.class, () -> NumberSupport.parseLong(null));
    assertThrows(NumberFormatException.class, () -> NumberSupport.parseLong(" "));
    assertThrows(NumberFormatException.class, () -> NumberSupport.parseLong("x"));
    assertThrows(NumberFormatException.class, () -> NumberSupport.parseLong("9223372036854775808"));
  }

  @Test
  void toDoubleReturnsParsedValueOrDefault() {
    assertEquals(123.5d, NumberSupport.toDouble("123.5"));
    assertEquals(123.5d, NumberSupport.toDouble(" 123.5 "));
    assertEquals(0.0d, NumberSupport.toDouble(null));
    assertEquals(7.5d, NumberSupport.toDouble("x", 7.5d));
    assertEquals(100.0d, NumberSupport.toDouble("1e2"));
    assertTrue(Double.isNaN(NumberSupport.toDouble("NaN")));
    assertEquals(Double.POSITIVE_INFINITY, NumberSupport.toDouble("Infinity"));
    assertEquals(7.0d, NumberSupport.toDouble("\u3000", 7.0d));
  }

  @Test
  void parseDoubleReturnsParsedValueOrThrows() {
    assertEquals(123.5d, NumberSupport.parseDouble("123.5"));
    assertEquals(123.5d, NumberSupport.parseDouble(" 123.5 "));
    assertThrows(NumberFormatException.class, () -> NumberSupport.parseDouble(null));
    assertThrows(NumberFormatException.class, () -> NumberSupport.parseDouble(" "));
    assertThrows(NumberFormatException.class, () -> NumberSupport.parseDouble("x"));
    assertTrue(Double.isNaN(NumberSupport.parseDouble("NaN")));
  }

  @Test
  void clampConstrainsIntValueToRange() {
    assertEquals(5, NumberSupport.clamp(5, 1, 10));
    assertEquals(1, NumberSupport.clamp(-1, 1, 10));
    assertEquals(10, NumberSupport.clamp(11, 1, 10));
    assertEquals(7, NumberSupport.clamp(7, 7, 7));
    assertEquals(
        Integer.MAX_VALUE,
        NumberSupport.clamp(Integer.MAX_VALUE, Integer.MIN_VALUE, Integer.MAX_VALUE));
  }

  @Test
  void clampConstrainsLongValueToRange() {
    assertEquals(5L, NumberSupport.clamp(5L, 1L, 10L));
    assertEquals(1L, NumberSupport.clamp(-1L, 1L, 10L));
    assertEquals(10L, NumberSupport.clamp(11L, 1L, 10L));
    assertEquals(0L, NumberSupport.clamp(0L, 0L, 0L));
  }

  @Test
  void clampConstrainsDoubleValueToRange() {
    assertEquals(5.5d, NumberSupport.clamp(5.5d, 1.0d, 10.0d));
    assertEquals(1.0d, NumberSupport.clamp(-1.0d, 1.0d, 10.0d));
    assertEquals(10.0d, NumberSupport.clamp(11.0d, 1.0d, 10.0d));
    assertEquals(2.5d, NumberSupport.clamp(2.5d, 2.5d, 2.5d));
  }

  @Test
  void clampRejectsInvalidRange() {
    final IllegalArgumentException intBad =
        assertThrows(IllegalArgumentException.class, () -> NumberSupport.clamp(1, 10, 1));
    assertTrue(intBad.getMessage().contains("min"));
    assertThrows(IllegalArgumentException.class, () -> NumberSupport.clamp(1L, 10L, 1L));
    assertThrows(IllegalArgumentException.class, () -> NumberSupport.clamp(1.0d, 10.0d, 1.0d));
  }

  @Test
  void clampDoublePropagatesNaNInValue() {
    assertTrue(Double.isNaN(NumberSupport.clamp(Double.NaN, 0.0d, 10.0d)));
  }

  @Test
  void clampDoubleWithNaNBoundsFollowsMathMinMax() {
    assertTrue(Double.isNaN(NumberSupport.clamp(5.0d, Double.NaN, 10.0d)));
    assertTrue(Double.isNaN(NumberSupport.clamp(5.0d, 0.0d, Double.NaN)));
  }

  @Test
  void parseIntDoesNotAcceptHexadecimalLiteral() {
    assertEquals(7, NumberSupport.toInt("0x10", 7));
    assertThrows(NumberFormatException.class, () -> NumberSupport.parseInt("0x10"));
  }

  @Test
  void parseLongAndIntRejectFloatingPointForm() {
    assertThrows(NumberFormatException.class, () -> NumberSupport.parseLong("1.0"));
    assertThrows(NumberFormatException.class, () -> NumberSupport.parseInt("1e2"));
  }

  @Test
  void isDigitsFalseWhenOnlySignOrDot() {
    assertFalse(NumberSupport.isDigits("+"));
    assertFalse(NumberSupport.isDigits("-"));
    assertFalse(NumberSupport.isDigits("."));
  }

  @Test
  void isParsableFalseWhenLeadingOrTrailingNonTrimmedJunk() {
    assertFalse(NumberSupport.isParsable("1\u3000"));
    assertTrue(NumberSupport.isParsable("1 "));
  }

  @Test
  void clampDoubleDoesNotThrowWhenMinOrMaxIsNaN() {
    assertDoesNotThrow(() -> NumberSupport.clamp(1.0d, Double.NaN, 10.0d));
    assertDoesNotThrow(() -> NumberSupport.clamp(1.0d, 0.0d, Double.NaN));
  }

  @Test
  void toIntOrDefaultNeverThrows() {
    assertEquals(-3, NumberSupport.toInt("not-a-number", -3));
  }
}
