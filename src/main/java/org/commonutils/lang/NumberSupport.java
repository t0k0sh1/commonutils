package org.commonutils.lang;

import java.util.Objects;
import org.commonutils.annotation.NonNull;
import org.commonutils.annotation.Nullable;

/**
 * Static helpers for numeric strings and simple bounds: digit checks, JDK-compatible parsing with
 * optional defaults, and {@code clamp}. Use with {@link StringSupport#trimToNull(String)} semantics
 * for consistent trimming of string inputs.
 *
 * <h2 id="trim">Trimming</h2>
 *
 * String-taking methods that parse numbers normalize input with {@link
 * StringSupport#trimToNull(String)} (JDK {@link String#trim()}, not Unicode {@link
 * String#strip()}). {@code null} or blank-after-trim is treated as missing input for {@code to*}
 * methods (default returned) and {@link #isParsable(String)} ({@code false}). {@link
 * #isDigits(CharSequence)} uses {@link StringSupport#isEmpty(CharSequence)} and does not trim;
 * leading or trailing spaces therefore yield {@code false}.
 *
 * <h2 id="to-vs-parse">Lenient {@code to*} vs strict {@code parse*}</h2>
 *
 * <dl>
 *   <dt>{@code toInt} / {@code toLong} / {@code toDouble}
 *   <dd>Never throw for bad input: missing or unparseable values return the overload default (or
 *       zero for single-argument overloads).
 *   <dt>{@code parseInt} / {@code parseLong} / {@code parseDouble}
 *   <dd>Throw {@link NumberFormatException} when the value is {@code null}, blank after trim, or
 *       not parseable by the corresponding JDK {@code parse*} method on the trimmed string.
 * </dl>
 *
 * <h2 id="parsable-vs-digits">{@code isParsable} vs {@code isDigits}</h2>
 *
 * {@link #isParsable(String)} returns {@code true} exactly when {@link Double#parseDouble(String)}
 * succeeds on the trimmed string (including hexadecimal floats, signed values, exponent notation,
 * and the literals {@code "NaN"} and infinities as accepted by the JDK). {@link
 * #isDigits(CharSequence)} requires every character to satisfy {@link Character#isDigit(char)} (no
 * sign, no decimal point, no spaces).
 *
 * <h2 id="clamp">{@code clamp}</h2>
 *
 * For primitive overloads, {@code min} must be {@code <= max} or {@link IllegalArgumentException}
 * is thrown. For {@link #clamp(double, double, double)}, bounds and value follow {@link Math#max}
 * and {@link Math#min}: a {@code NaN} value stays {@code NaN}; if {@code min} or {@code max} is
 * {@code NaN}, range validation may not reject the pair (because comparisons with {@code NaN} are
 * false).
 *
 * @see StringSupport#trimToNull(String)
 * @see Double#parseDouble(String)
 *     <p><strong id="nullability">Nullability annotations</strong> ({@link
 *     org.commonutils.annotation.Nullable}, {@link org.commonutils.annotation.NonNull}) document
 *     contracts on signatures; {@link org.commonutils.annotation.NonNull}-annotated reference
 *     parameters are validated at runtime with {@link java.lang.NullPointerException} where
 *     applicable.
 *     <p><strong id="numeric-contracts">Numeric constraints</strong> ({@link
 *     org.commonutils.annotation.NonNegative}, {@link org.commonutils.annotation.Positive})
 *     document ranges on signatures; implementations enforce them at runtime with {@link
 *     IllegalArgumentException}.
 */
public final class NumberSupport {
  private NumberSupport() {}

  /**
   * Returns {@code true} if {@code value} is non-null, non-empty, and every code unit is a decimal
   * digit per {@link Character#isDigit(char)}. Does not trim: surrounding whitespace makes the
   * result {@code false}. Signs, decimal points, and grouping separators are not allowed.
   *
   * @param value sequence to test, may be {@code null}
   * @return {@code true} if {@code value} is non-empty and all characters are digits
   * @see #isParsable(String)
   */
  public static boolean isDigits(final @Nullable CharSequence value) {
    if (StringSupport.isEmpty(value)) {
      return false;
    }

    for (int i = 0; i < value.length(); i++) {
      if (!Character.isDigit(value.charAt(i))) {
        return false;
      }
    }

    return true;
  }

  /**
   * Returns {@code true} if {@code value} is non-null, not blank after {@link
   * StringSupport#trimToNull(String)}, and {@link Double#parseDouble(String)} accepts the trimmed
   * string. This matches JDK parsing rules (including signed values, exponent notation, and {@code
   * "NaN"} / infinity literals).
   *
   * @param value string to test, may be {@code null}
   * @return {@code true} if trimmed {@code value} parses as a {@code double}
   * @see #isDigits(CharSequence)
   */
  public static boolean isParsable(final @Nullable String value) {
    final String normalized = StringSupport.trimToNull(value);
    if (ObjectSupport.isNull(normalized)) {
      return false;
    }

    try {
      Double.parseDouble(normalized);
      return true;
    } catch (final NumberFormatException ignored) {
      return false;
    }
  }

  /**
   * Parses {@code value} as a signed decimal integer, or returns {@code 0} when missing or invalid.
   * Equivalent to {@link #toInt(String, int) toInt}{@code (value, 0)}.
   *
   * @param value string to parse, may be {@code null}
   * @return parsed {@code int}, or {@code 0} when null, blank after trim, or not parseable
   * @see #toInt(String, int)
   * @see #parseInt(String)
   */
  public static int toInt(final @Nullable String value) {
    return toInt(value, 0);
  }

  /**
   * Parses {@code value} as a signed decimal integer after trim, or throws if missing or invalid.
   * Delegates to {@link Integer#parseInt(String)} on the trimmed string.
   *
   * @param value string to parse, may be {@code null}
   * @return the parsed {@code int}
   * @throws NumberFormatException if {@code value} is {@code null}, blank after trim, or not a
   *     valid {@code int}
   * @see #toInt(String, int)
   */
  public static int parseInt(final @Nullable String value) {
    return Integer.parseInt(requireParsableValue(value));
  }

  /**
   * Parses {@code value} as a signed decimal integer after {@link
   * StringSupport#trimToNull(String)}, or returns {@code defaultValue} when {@code null}, blank
   * after trim, or not parseable.
   *
   * @param value string to parse, may be {@code null}
   * @param defaultValue value when input is missing or invalid
   * @return parsed {@code int} or {@code defaultValue}
   * @see #parseInt(String)
   */
  public static int toInt(final @Nullable String value, final int defaultValue) {
    final String normalized = StringSupport.trimToNull(value);
    if (ObjectSupport.isNull(normalized)) {
      return defaultValue;
    }

    return parseIntOrDefault(normalized, defaultValue);
  }

  /**
   * Parses {@code value} as a signed decimal long, or returns {@code 0L} when missing or invalid.
   * Equivalent to {@link #toLong(String, long) toLong}{@code (value, 0L)}.
   *
   * @param value string to parse, may be {@code null}
   * @return parsed {@code long}, or {@code 0L} when null, blank after trim, or not parseable
   * @see #toLong(String, long)
   * @see #parseLong(String)
   */
  public static long toLong(final @Nullable String value) {
    return toLong(value, 0L);
  }

  /**
   * Parses {@code value} as a signed decimal long after trim, or throws if missing or invalid.
   * Delegates to {@link Long#parseLong(String)} on the trimmed string.
   *
   * @param value string to parse, may be {@code null}
   * @return the parsed {@code long}
   * @throws NumberFormatException if {@code value} is {@code null}, blank after trim, or not a
   *     valid {@code long}
   * @see #toLong(String, long)
   */
  public static long parseLong(final @Nullable String value) {
    return Long.parseLong(requireParsableValue(value));
  }

  /**
   * Parses {@code value} as a signed decimal long after {@link StringSupport#trimToNull(String)},
   * or returns {@code defaultValue} when {@code null}, blank after trim, or not parseable.
   *
   * @param value string to parse, may be {@code null}
   * @param defaultValue value when input is missing or invalid
   * @return parsed {@code long} or {@code defaultValue}
   * @see #parseLong(String)
   */
  public static long toLong(final @Nullable String value, final long defaultValue) {
    final String normalized = StringSupport.trimToNull(value);
    if (ObjectSupport.isNull(normalized)) {
      return defaultValue;
    }

    return parseLongOrDefault(normalized, defaultValue);
  }

  /**
   * Parses {@code value} as a {@code double}, or returns {@code 0.0} when missing or invalid.
   * Equivalent to {@link #toDouble(String, double) toDouble}{@code (value, 0.0d)}.
   *
   * @param value string to parse, may be {@code null}
   * @return parsed {@code double}, or {@code 0.0} when null, blank after trim, or not parseable
   * @see #toDouble(String, double)
   * @see #parseDouble(String)
   */
  public static double toDouble(final @Nullable String value) {
    return toDouble(value, 0.0d);
  }

  /**
   * Parses {@code value} as a {@code double} after trim, or throws if missing or invalid. Delegates
   * to {@link Double#parseDouble(String)} on the trimmed string.
   *
   * @param value string to parse, may be {@code null}
   * @return the parsed {@code double}
   * @throws NumberFormatException if {@code value} is {@code null}, blank after trim, or not a
   *     valid {@code double}
   * @see #toDouble(String, double)
   */
  public static double parseDouble(final @Nullable String value) {
    return Double.parseDouble(requireParsableValue(value));
  }

  /**
   * Parses {@code value} as a {@code double} after {@link StringSupport#trimToNull(String)}, or
   * returns {@code defaultValue} when {@code null}, blank after trim, or not parseable.
   *
   * @param value string to parse, may be {@code null}
   * @param defaultValue value when input is missing or invalid
   * @return parsed {@code double} or {@code defaultValue}
   * @see #parseDouble(String)
   */
  public static double toDouble(final @Nullable String value, final double defaultValue) {
    final String normalized = StringSupport.trimToNull(value);
    if (ObjectSupport.isNull(normalized)) {
      return defaultValue;
    }

    return parseDoubleOrDefault(normalized, defaultValue);
  }

  /**
   * Returns {@code value} limited to the inclusive range {@code [min, max]}.
   *
   * @param value value to constrain
   * @param min inclusive lower bound
   * @param max inclusive upper bound (must be {@code >= min})
   * @return {@code value} clamped to {@code [min, max]}
   * @throws IllegalArgumentException if {@code min > max}
   */
  public static int clamp(final int value, final int min, final int max) {
    validateClampRange(min, max);
    return Math.max(min, Math.min(value, max));
  }

  /**
   * Returns {@code value} limited to the inclusive range {@code [min, max]}.
   *
   * @param value value to constrain
   * @param min inclusive lower bound
   * @param max inclusive upper bound (must be {@code >= min})
   * @return {@code value} clamped to {@code [min, max]}
   * @throws IllegalArgumentException if {@code min > max}
   */
  public static long clamp(final long value, final long min, final long max) {
    validateClampRange(min, max);
    return Math.max(min, Math.min(value, max));
  }

  /**
   * Returns {@code value} limited to the inclusive range {@code [min, max]}, using {@link
   * Math#max(double, double)} and {@link Math#min(double, double)}. If {@code value} is {@code
   * NaN}, the result is {@code NaN}. If {@code min} or {@code max} is {@code NaN}, validation may
   * not throw even when the interval is not sensible; see class documentation.
   *
   * @param value value to constrain
   * @param min inclusive lower bound
   * @param max inclusive upper bound (must be {@code >= min} when both are finite and ordered)
   * @return {@code value} clamped to {@code [min, max]}
   * @throws IllegalArgumentException if {@code min > max} (never true when either is {@code NaN})
   */
  public static double clamp(final double value, final double min, final double max) {
    validateClampRange(min, max);
    return Math.max(min, Math.min(value, max));
  }

  private static void validateClampRange(final double min, final double max) {
    if (min > max) {
      throw new IllegalArgumentException("min must be less than or equal to max");
    }
  }

  private static @NonNull String requireParsableValue(final @Nullable String value) {
    final String normalized = StringSupport.trimToNull(value);
    if (ObjectSupport.isNull(normalized)) {
      throw new NumberFormatException("value must not be null or blank");
    }

    return normalized;
  }

  private static int parseIntOrDefault(final @NonNull String normalized, final int defaultValue) {
    Objects.requireNonNull(normalized, "normalized");
    try {
      return Integer.parseInt(normalized);
    } catch (final NumberFormatException ignored) {
      return defaultValue;
    }
  }

  private static long parseLongOrDefault(
      final @NonNull String normalized, final long defaultValue) {
    Objects.requireNonNull(normalized, "normalized");
    try {
      return Long.parseLong(normalized);
    } catch (final NumberFormatException ignored) {
      return defaultValue;
    }
  }

  private static double parseDoubleOrDefault(
      final @NonNull String normalized, final double defaultValue) {
    Objects.requireNonNull(normalized, "normalized");
    try {
      return Double.parseDouble(normalized);
    } catch (final NumberFormatException ignored) {
      return defaultValue;
    }
  }
}
