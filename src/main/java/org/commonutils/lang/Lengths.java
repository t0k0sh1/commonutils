package org.commonutils.lang;

import org.commonutils.annotation.NonNegative;
import org.commonutils.annotation.Nullable;
import org.commonutils.internal.EastAsianWidthColumns;

/**
 * Built-in {@link LengthCounter} strategies. Use with {@link StringSupport#length(CharSequence,
 * LengthCounter)}.
 *
 * <p><strong>{@link #UNITS}</strong> counts UTF-16 code units (same as {@link
 * CharSequence#length()} when the sequence is non-null). <strong>{@link #POINTS}</strong> counts
 * Unicode code points; supplementary characters (e.g. emoji) count once. It is <em>not</em>
 * grapheme-cluster (visual character) counting. <strong>{@link #EAW}</strong> sums display columns
 * using Unicode East Asian Width (UAX #11): categories F, W, and A count as 2 columns; H, Na, and N
 * as 1. Unlisted code points are treated as N (width 1). Data: Unicode 15.1.0 {@code
 * EastAsianWidth.txt}.
 */
public enum Lengths implements LengthCounter {
  /**
   * UTF-16 code units: non-{@code null} sequences use {@link CharSequence#length()}; {@code null}
   * counts as {@code 0}.
   */
  UNITS {
    @Override
    @NonNegative
    public int count(final @Nullable CharSequence sequence) {
      return ObjectSupport.isNull(sequence) ? 0 : sequence.length();
    }
  },

  /**
   * Unicode code point count (including astral planes as one each). Not grapheme-cluster length.
   */
  POINTS {
    @Override
    @NonNegative
    public int count(final @Nullable CharSequence sequence) {
      if (ObjectSupport.isNull(sequence)) {
        return 0;
      }
      int n = 0;
      for (int i = 0; i < sequence.length(); ) {
        final int cp = Character.codePointAt(sequence, i);
        i += Character.charCount(cp);
        n++;
      }
      return n;
    }
  },

  /**
   * Sum of East Asian display column widths (typically half-width 1, full-wide 2). Ambiguous (A) is
   * counted as width 2.
   */
  EAW {
    @Override
    @NonNegative
    public int count(final @Nullable CharSequence sequence) {
      if (ObjectSupport.isNull(sequence)) {
        return 0;
      }
      int total = 0;
      for (int i = 0; i < sequence.length(); ) {
        final int cp = Character.codePointAt(sequence, i);
        total += EastAsianWidthColumns.columnWidth(cp);
        i += Character.charCount(cp);
      }
      return total;
    }
  };
}
