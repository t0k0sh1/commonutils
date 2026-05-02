package org.commonutils.lang;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.commonutils.internal.EastAsianWidthColumns;
import org.junit.jupiter.api.Test;

/**
 * Edge-case coverage for {@link StringSupport#length(CharSequence, LengthCounter)} and {@link
 * Lengths} (UTF-16 units, code points, East Asian columns).
 */
class StringSupportLengthEdgeCasesTest {

  @Test
  void emptyStringIsZeroForAllStrategies() {
    assertEquals(0, StringSupport.length(""));
    assertEquals(0, StringSupport.length("", Lengths.UNITS));
    assertEquals(0, StringSupport.length("", Lengths.POINTS));
    assertEquals(0, StringSupport.length("", Lengths.EAW));
    assertEquals(0, Lengths.UNITS.count(new StringBuilder()));
    assertEquals(0, Lengths.POINTS.count(new StringBuffer()));
  }

  @Test
  void unitsAlwaysMatchesCharSequenceLengthIncludingBrokenSurrogates() {
    assertEquals(1, StringSupport.length("\uD800"));
    assertEquals(1, StringSupport.length("\uDC00"));
    assertEquals(4, StringSupport.length("\uD83D\uDE00\uD83D\uDE00"));
    assertEquals(2, StringSupport.length("\uDE00\uD83D"));
  }

  @Test
  void codePointsCountSupplementaryPairsOnceAndIsolateIllFormedUnits() {
    assertEquals(1, StringSupport.length("\uD800", Lengths.POINTS));
    assertEquals(1, StringSupport.length("\uDC00", Lengths.POINTS));
    assertEquals(2, StringSupport.length("\uDE00\uD83D", Lengths.POINTS));
    assertEquals(2, StringSupport.length("\uD83D\uDE00a", Lengths.POINTS));
    assertEquals(2, StringSupport.length("\uD83D\uDE00\uD83D\uDE00", Lengths.POINTS));
  }

  @Test
  void codePointsMatchesStringCodePointCountWhenWellFormed() {
    final String s = "a\uD83D\uDE00b\u30A2";
    assertEquals(s.codePointCount(0, s.length()), StringSupport.length(s, Lengths.POINTS));
  }

  @Test
  void combiningCharactersCountAsSeparateCodePoints() {
    final String composed = "e\u0301";
    assertEquals(2, StringSupport.length(composed));
    assertEquals(2, StringSupport.length(composed, Lengths.POINTS));
  }

  @Test
  void regionalIndicatorPairsAreTwoCodePointsFourCodeUnits() {
    final String flag = "\uD83C\uDDFA\uD83C\uDDF8";
    assertEquals(4, StringSupport.length(flag, Lengths.UNITS));
    assertEquals(2, StringSupport.length(flag, Lengths.POINTS));
    assertEquals(2, StringSupport.length(flag, Lengths.EAW));
  }

  @Test
  void emojiWithEmojiPresentationSelectorAddsBmpCodeUnitAndEawColumns() {
    final String withVs = "\uD83D\uDE00\uFE0F";
    assertEquals(3, StringSupport.length(withVs, Lengths.UNITS));
    assertEquals(2, StringSupport.length(withVs, Lengths.POINTS));
    assertEquals(4, StringSupport.length(withVs, Lengths.EAW));
  }

  @Test
  void fullwidthDigitHasEawWidthTwoHalfwidthKatakanaWidthOne() {
    assertEquals(2, StringSupport.length("\uFF11", Lengths.EAW));
    assertEquals(1, StringSupport.length("\uFF66", Lengths.EAW));
  }

  @Test
  void neutralAndNarrowAsciiControlEawWidthOne() {
    assertEquals(1, StringSupport.length("\u200B", Lengths.EAW));
    assertEquals(1, StringSupport.length("\n", Lengths.EAW));
  }

  @Test
  void unlistedCodePointFallsBackToNeutralWidthOne() {
    assertEquals(1, EastAsianWidthColumns.columnWidth(0x0378));
    assertEquals(1, StringSupport.length("\u0378", Lengths.EAW));
  }

  @Test
  void invalidCodePointYieldsColumnWidthOne() {
    assertEquals(1, EastAsianWidthColumns.columnWidth(0xD800));
    assertEquals(1, EastAsianWidthColumns.columnWidth(0x110000));
  }

  @Test
  void eawIterationMatchesWalkingCodePointsOnMessyUtf16() {
    final String messy = "x\uD800\uD83D\uDE00\uDC00";
    int manual = 0;
    for (int i = 0; i < messy.length(); ) {
      final int cp = Character.codePointAt(messy, i);
      manual += EastAsianWidthColumns.columnWidth(cp);
      i += Character.charCount(cp);
    }
    assertEquals(manual, StringSupport.length(messy, Lengths.EAW));
  }

  @Test
  void unitsStrategyMatchesDefaultLengthOnCharSequenceImplementations() {
    final CharSequence[] samples = {
      "", "plain", "\uD83D\uDE00", new StringBuilder("x\u30A2"), new StringBuffer("\uD800"),
    };
    for (final CharSequence s : samples) {
      assertEquals(StringSupport.length(s), StringSupport.length(s, Lengths.UNITS));
      assertEquals(StringSupport.length(s), Lengths.UNITS.count(s));
    }
  }
}
