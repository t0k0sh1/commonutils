package org.commonutils.lang;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class StringSupportTest {
  @Test
  void isEmptyReturnsTrueForNullOrEmpty() {
    assertTrue(StringSupport.isEmpty(null));
    assertTrue(StringSupport.isEmpty(""));
    assertFalse(StringSupport.isEmpty(" "));
    assertFalse(StringSupport.isEmpty("abc"));
    assertTrue(StringSupport.isEmpty(new StringBuilder()));
    assertFalse(StringSupport.isEmpty(new StringBuilder(" ")));
  }

  @Test
  void isNotEmptyReturnsTrueOnlyWhenValueIsNotNullAndNotEmpty() {
    assertFalse(StringSupport.isNotEmpty(null));
    assertFalse(StringSupport.isNotEmpty(""));
    assertTrue(StringSupport.isNotEmpty(" "));
    assertTrue(StringSupport.isNotEmpty("abc"));
  }

  @Test
  void isBlankReturnsTrueOnlyWhenValueHasNoVisibleCharacters() {
    assertTrue(StringSupport.isBlank(null));
    assertTrue(StringSupport.isBlank(""));
    assertTrue(StringSupport.isBlank(" \t\n"));
    assertTrue(StringSupport.isBlank(new StringBuilder(" \t")));
    assertFalse(StringSupport.isBlank(" a "));
  }

  @Test
  void isNotBlankReturnsTrueOnlyWhenValueContainsVisibleCharacters() {
    assertFalse(StringSupport.isNotBlank(null));
    assertFalse(StringSupport.isNotBlank(""));
    assertFalse(StringSupport.isNotBlank(" \t\n"));
    assertTrue(StringSupport.isNotBlank(" a "));
  }

  @Test
  void defaultStringReplacesNullOnly() {
    assertEquals("", StringSupport.defaultString(null));
    assertEquals("value", StringSupport.defaultString("value"));
    assertEquals("fallback", StringSupport.defaultString(null, "fallback"));
    assertEquals("", StringSupport.defaultString("", "fallback"));
    assertEquals("x", StringSupport.defaultString("x", "ignored"));
    assertThrows(NullPointerException.class, () -> StringSupport.defaultString(null, null));
    assertEquals("", StringSupport.defaultString("", ""));
    assertNotNull(StringSupport.defaultString(null));
    assertSame("", StringSupport.defaultString(null));
  }

  @Test
  void equalsReturnsTrueOnlyWhenContentsMatch() {
    assertTrue(StringSupport.equals(null, null));
    assertTrue(StringSupport.equals("abc", "abc"));
    assertFalse(StringSupport.equals(null, "abc"));
    assertFalse(StringSupport.equals("abc", "ABC"));
    assertFalse(StringSupport.equals("abc", "abcd"));
    assertTrue(StringSupport.equals(new StringBuilder("xy"), "xy"));
    assertTrue(StringSupport.equals("xy", new StringBuilder("xy")));
    assertFalse(StringSupport.equals(new StringBuilder("x"), "xy"));
    assertTrue(StringSupport.equals("\uD83D\uDE00", "\uD83D\uDE00"));
  }

  @Test
  void equalsIgnoreCaseIgnoresCharacterCase() {
    assertTrue(StringSupport.equalsIgnoreCase(null, null));
    assertTrue(StringSupport.equalsIgnoreCase("abc", "ABC"));
    assertTrue(StringSupport.equalsIgnoreCase("AbC", "aBc"));
    assertFalse(StringSupport.equalsIgnoreCase(null, "abc"));
    assertFalse(StringSupport.equalsIgnoreCase("abc", "abd"));
    assertTrue(StringSupport.equalsIgnoreCase(new StringBuilder("gzip"), "GZIP"));
  }

  @Test
  void defaultIfEmptyReplacesOnlyNullOrEmpty() {
    assertEquals("fallback", StringSupport.defaultIfEmpty(null, "fallback"));
    assertEquals("fallback", StringSupport.defaultIfEmpty("", "fallback"));
    assertEquals(" ", StringSupport.defaultIfEmpty(" ", "fallback"));
    assertEquals("value", StringSupport.defaultIfEmpty("value", "fallback"));
    assertEquals("value", StringSupport.defaultIfEmpty("value", null));
    assertNull(StringSupport.defaultIfEmpty(null, null));
    assertNull(StringSupport.defaultIfEmpty("", null));
  }

  @Test
  void defaultIfBlankReplacesNullEmptyOrWhitespaceOnly() {
    assertEquals("fallback", StringSupport.defaultIfBlank(null, "fallback"));
    assertEquals("fallback", StringSupport.defaultIfBlank("", "fallback"));
    assertEquals("fallback", StringSupport.defaultIfBlank(" \t", "fallback"));
    assertEquals("value", StringSupport.defaultIfBlank("value", "fallback"));
    assertEquals("value", StringSupport.defaultIfBlank("value", null));
    assertNull(StringSupport.defaultIfBlank(null, null));
    assertNull(StringSupport.defaultIfBlank("  ", null));
  }

  @Test
  void padStartPadsBeforeContentAndTreatsNullAsEmpty() {
    assertEquals("", StringSupport.padStart(null, 0, ' '));
    assertEquals("     ", StringSupport.padStart(null, 5, ' '));
    assertEquals("00042", StringSupport.padStart("42", 5, '0'));
    assertEquals("hello", StringSupport.padStart("hello", 3, 'x'));
    assertEquals("hello", StringSupport.padStart("hello", 5, 'z'));
    assertEquals("", StringSupport.padStart(null, -3, 'x'));
    assertEquals("abc", StringSupport.padStart("abc", 3, '0'));
  }

  @Test
  void padEndPadsAfterContentAndTreatsNullAsEmpty() {
    assertEquals("", StringSupport.padEnd(null, 0, '-'));
    assertEquals("-----", StringSupport.padEnd(null, 5, '-'));
    assertEquals("hi---", StringSupport.padEnd("hi", 5, '-'));
    assertEquals("hello", StringSupport.padEnd("hello", 3, '.'));
    assertEquals("hello", StringSupport.padEnd("hello", 5, '.'));
    assertEquals("", StringSupport.padEnd(null, -1, '.'));
  }

  @Test
  void trimToEmptyReturnsEmptyStringForNullAndTrimmedValueOtherwise() {
    assertEquals("", StringSupport.trimToEmpty(null));
    assertEquals("", StringSupport.trimToEmpty("   "));
    assertEquals("abc", StringSupport.trimToEmpty("  abc  "));
    assertEquals("", StringSupport.trimToEmpty("\t\r\n"));
    assertEquals("", StringSupport.trimToEmpty("\u001C"));
  }

  @Test
  void trimToNullRemovesEdgeWhitespaceAndCollapsesBlankToNull() {
    assertNull(StringSupport.trimToNull(null));
    assertNull(StringSupport.trimToNull(""));
    assertNull(StringSupport.trimToNull("   "));
    assertEquals("abc", StringSupport.trimToNull("  abc  "));
  }

  @Test
  void stripToEmptyUsesUnicodeWhitespaceIncludingIdeographicSpace() {
    assertEquals("", StringSupport.stripToEmpty(null));
    assertEquals("", StringSupport.stripToEmpty("   "));
    assertEquals("", StringSupport.stripToEmpty("\u3000"));
    assertEquals("abc", StringSupport.stripToEmpty("  abc  "));
    assertEquals("abc", StringSupport.stripToEmpty("\u3000abc\u3000"));
  }

  @Test
  void stripToNullStripsUnicodeWhitespaceToNull() {
    assertNull(StringSupport.stripToNull(null));
    assertNull(StringSupport.stripToNull(""));
    assertNull(StringSupport.stripToNull("   "));
    assertNull(StringSupport.stripToNull("\u3000"));
    assertEquals("abc", StringSupport.stripToNull("  abc  "));
  }

  @Test
  void trimDoesNotRemoveIdeographicSpaceWhereStripDoes() {
    assertEquals("\u3000", StringSupport.trimToEmpty("\u3000"));
    assertEquals("\u3000", StringSupport.trimToNull("\u3000"));
    assertEquals("", StringSupport.stripToEmpty("\u3000"));
    assertNull(StringSupport.stripToNull("\u3000"));
  }

  @Test
  void lengthReturnsZeroForNullSequence() {
    assertEquals(0, StringSupport.length(null));
    assertEquals(0, StringSupport.length(null, Lengths.POINTS));
    assertEquals(0, StringSupport.length(null, Lengths.EAW));
    assertEquals(0, StringSupport.length(null, Lengths.UNITS));
  }

  @Test
  void lengthRejectsNullCounter() {
    assertThrows(NullPointerException.class, () -> StringSupport.length("a", null));
  }

  @Test
  void lengthRejectsNegativeCountFromCustomCounter() {
    final LengthCounter bad = s -> -1;
    final IllegalArgumentException ex =
        assertThrows(IllegalArgumentException.class, () -> StringSupport.length("", bad));
    assertTrue(ex.getMessage().contains("LengthCounter.count"));
  }

  @Test
  void lengthAsciiMatchesAllBuiltinStrategies() {
    assertEquals(3, StringSupport.length("abc"));
    assertEquals(3, StringSupport.length("abc", Lengths.UNITS));
    assertEquals(3, StringSupport.length("abc", Lengths.POINTS));
    assertEquals(3, StringSupport.length("abc", Lengths.EAW));
  }

  @Test
  void lengthEmojiSurrogatePair() {
    final String grinning = "\uD83D\uDE00";
    assertEquals(2, StringSupport.length(grinning));
    assertEquals(2, StringSupport.length(grinning, Lengths.UNITS));
    assertEquals(1, StringSupport.length(grinning, Lengths.POINTS));
    assertEquals(2, StringSupport.length(grinning, Lengths.EAW));
  }

  @Test
  void lengthMixedHalfAndFullWidth() {
    assertEquals(2, StringSupport.length("a\u30A2"));
    assertEquals(2, StringSupport.length("a\u30A2", Lengths.POINTS));
    assertEquals(3, StringSupport.length("a\u30A2", Lengths.EAW));
  }

  @Test
  void lengthAmbiguousGreekCountsAsTwoColumns() {
    assertEquals(2, StringSupport.length("\u0391", Lengths.EAW));
    assertEquals(1, StringSupport.length("\u0391", Lengths.POINTS));
  }

  @Test
  void customLengthCounter() {
    final LengthCounter zeroOrOne = seq -> StringSupport.isEmpty(seq) ? 0 : 1;
    assertEquals(0, StringSupport.length(null, zeroOrOne));
    assertEquals(0, StringSupport.length("", zeroOrOne));
    assertEquals(1, StringSupport.length("x", zeroOrOne));
  }

  @Test
  void lengthWorksOnStringBuilder() {
    final StringBuilder sb = new StringBuilder("a\u30A2");
    assertEquals(2, StringSupport.length(sb, Lengths.POINTS));
    assertEquals(3, Lengths.EAW.count(sb));
  }
}
