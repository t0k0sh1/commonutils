package org.commonutils.lang;

import org.commonutils.annotation.NonNull;
import org.commonutils.annotation.Nullable;
import org.commonutils.annotation.Positive;
import org.commonutils.internal.Contracts;

/**
 * Static helpers for {@link String} and {@link CharSequence} work: presence checks, safe defaults,
 * content equality, and trimming helpers. Use this class when you want consistent, null-tolerant
 * behavior across the codebase without repeating the same guards.
 *
 * <h2 id="null-handling">Null handling</h2>
 *
 * Unless stated otherwise, {@code null} references are treated as &quot;missing&quot;:
 *
 * <ul>
 *   <li>Predicates such as {@link #isEmpty(CharSequence)} and {@link #isBlank(CharSequence)} treat
 *       {@code null} like an empty or whitespace-only value.
 *   <li>Methods that return a {@link String} often map {@code null} to {@code ""} or a supplied
 *       default; see each method for exact rules.
 * </ul>
 *
 * This matches common utility conventions (and differs from calling {@link String} instance methods
 * on {@code null}, which would throw {@link NullPointerException}).
 *
 * <h2 id="empty-vs-blank">Empty vs blank</h2>
 *
 * <dl>
 *   <dt>{@link #isEmpty(CharSequence) Empty}
 *   <dd>{@code null} or length zero only. A string of spaces {@code " "} is <em>not</em> empty.
 *   <dt>{@link #isBlank(CharSequence) Blank}
 *   <dd>{@code null}, empty, or every character satisfies {@link Character#isWhitespace(char)}. Use
 *       this for user-facing input where &quot;only spaces&quot; should count as no input.
 * </dl>
 *
 * Pair with {@link #defaultIfEmpty(String, String)} vs {@link #defaultIfBlank(String, String)}
 * depending on whether spaces-only should fall back to a default.
 *
 * <h2 id="padding">Padding</h2>
 *
 * {@link #padStart(String, int, char)} and {@link #padEnd(String, int, char)} repeat {@code
 * paddingChar} until {@link String#length()} is at least {@code minLength} (same roles as {@code
 * leftPad}/{@code rightPad} or JDK {@code String.padStart}/{@code padEnd} on releases that ship
 * those methods). A {@code null} {@code value} is treated as {@code ""} before padding.
 *
 * <h2 id="trim-vs-strip">Trim vs strip</h2>
 *
 * {@link #trimToEmpty(String)} and {@link #trimToNull(String)} delegate to {@link String#trim()},
 * which removes only leading/trailing characters with codepoint at most U+0020 (space and related
 * ASCII control characters). They do <em>not</em> remove e.g. ideographic space U+3000.
 *
 * <p>{@link #stripToEmpty(String)} and {@link #stripToNull(String)} delegate to {@link
 * String#strip()}, which removes Unicode whitespace at both ends (including U+3000). Prefer {@code
 * strip*} for normalized user input; keep {@code trim*} when you need JDK {@code trim()} semantics
 * or backward compatibility.
 *
 * <h2 id="equality">Equality</h2>
 *
 * {@link #equals(CharSequence, CharSequence)} and {@link #equalsIgnoreCase(CharSequence,
 * CharSequence)} compare <em>content</em> (code unit sequences), not object identity. {@code left
 * == right} is only a fast path. They are {@code null}-safe: two {@code null} references are
 * considered equal. {@link #equalsIgnoreCase(CharSequence, CharSequence)} uses per-character case
 * folding suitable for typical ASCII/Unicode strings; it is not a substitute for locale-sensitive
 * {@link java.text.Collator} comparisons.
 *
 * <h2 id="examples">Examples</h2>
 *
 * <pre>{@code
 * // Defaults after optional form field
 * String name = StringSupport.defaultIfBlank(rawName, "anonymous");
 *
 * // Parse-friendly normalization (Unicode spaces, including full-width)
 * String token = StringSupport.stripToNull(userInput);
 * if (StringSupport.isBlank(token)) {
 *     throw new IllegalArgumentException("token required");
 * }
 *
 * // Safe equality for CharSequence from different implementations
 * if (StringSupport.equalsIgnoreCase(headerValue, "gzip")) {
 *     // ...
 * }
 * }</pre>
 *
 * @see Character#isWhitespace(char)
 * @see String#trim()
 * @see String#strip()
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
public final class StringSupport {
  private StringSupport() {}

  /**
   * Returns {@code true} if {@code value} is {@code null} or {@link CharSequence#isEmpty()}. A
   * string containing only spaces is not empty.
   *
   * @param value sequence to test, may be {@code null}
   * @return {@code true} if {@code value} is {@code null} or has length 0
   * @see #isBlank(CharSequence)
   * @see #isNotEmpty(CharSequence)
   */
  public static boolean isEmpty(final @Nullable CharSequence value) {
    return ObjectSupport.isNull(value) || value.isEmpty();
  }

  /**
   * Negation of {@link #isEmpty(CharSequence)}: {@code true} when {@code value} is non-null and has
   * length greater than zero.
   *
   * @param value sequence to test, may be {@code null}
   * @return {@code true} if {@code value} is not {@code null} and not empty
   * @see #isNotBlank(CharSequence)
   */
  public static boolean isNotEmpty(final @Nullable CharSequence value) {
    return !isEmpty(value);
  }

  /**
   * Returns {@code true} if {@code value} is {@code null}, empty, or every code unit is Unicode
   * whitespace per {@link Character#isWhitespace(char)}. Use for &quot;nothing useful to show&quot;
   * checks on user input; for strict length-zero checks use {@link #isEmpty(CharSequence)}.
   *
   * @param value sequence to test, may be {@code null}
   * @return {@code true} if {@code value} is {@code null} or has no non-whitespace characters
   * @see #isEmpty(CharSequence)
   * @see #isNotBlank(CharSequence)
   */
  public static boolean isBlank(final @Nullable CharSequence value) {
    if (ObjectSupport.isNull(value)) {
      return true;
    }

    for (int i = 0; i < value.length(); i++) {
      if (!Character.isWhitespace(value.charAt(i))) {
        return false;
      }
    }

    return true;
  }

  /**
   * Negation of {@link #isBlank(CharSequence)}: {@code true} when {@code value} contains at least
   * one character that is not {@link Character#isWhitespace(char) whitespace}.
   *
   * @param value sequence to test, may be {@code null}
   * @return {@code true} if {@code value} has at least one non-whitespace character
   * @see #isNotEmpty(CharSequence)
   */
  public static boolean isNotBlank(final @Nullable CharSequence value) {
    return !isBlank(value);
  }

  /**
   * Returns {@code ""} when {@code value} is {@code null}; otherwise returns {@code value}
   * unchanged. Same as calling {@link #defaultString(String, String)} with {@code ""} as the
   * default.
   *
   * @param value input string, may be {@code null}
   * @return non-null string, never {@code null}
   * @see #defaultString(String, String)
   */
  public static @NonNull String defaultString(final @Nullable String value) {
    return defaultString(value, "");
  }

  /**
   * Returns {@code true} if both sequences have the same length and the same {@code char} values at
   * every index. {@code null} is handled safely: {@code null} equals only {@code null}.
   *
   * <p>Unlike {@link String#equals(Object)}, this method accepts any {@link CharSequence} (e.g.
   * {@link StringBuilder}) on either side, so you can compare formatted text without allocating a
   * new {@link String}.
   *
   * @param left first sequence, may be {@code null}
   * @param right second sequence, may be {@code null}
   * @return {@code true} if both are {@code null} or both have identical content
   * @see #equalsIgnoreCase(CharSequence, CharSequence)
   * @see org.commonutils.lang.ObjectSupport#equals(Object, Object)
   */
  public static boolean equals(
      final @Nullable CharSequence left, final @Nullable CharSequence right) {
    if (left == right) {
      return true;
    }
    if (ObjectSupport.isNull(left)
        || ObjectSupport.isNull(right)
        || left.length() != right.length()) {
      return false;
    }

    for (int i = 0; i < left.length(); i++) {
      if (left.charAt(i) != right.charAt(i)) {
        return false;
      }
    }

    return true;
  }

  /**
   * Like {@link #equals(CharSequence, CharSequence)}, but compares case-insensitively using {@link
   * Character#toUpperCase(char)} and {@link Character#toLowerCase(char)} on each pair of
   * characters. Suitable for protocol tokens, HTTP headers, and ASCII identifiers; for
   * locale-sensitive natural-language equality, consider {@link java.text.Collator} instead.
   *
   * @param left first sequence, may be {@code null}
   * @param right second sequence, may be {@code null}
   * @return {@code true} if both are {@code null} or both have the same length and match
   *     case-insensitively at each index
   * @see String#equalsIgnoreCase(String)
   */
  public static boolean equalsIgnoreCase(
      final @Nullable CharSequence left, final @Nullable CharSequence right) {
    if (left == right) {
      return true;
    }
    if (ObjectSupport.isNull(left)
        || ObjectSupport.isNull(right)
        || left.length() != right.length()) {
      return false;
    }

    for (int i = 0; i < left.length(); i++) {
      if (!charsEqualIgnoreCase(left.charAt(i), right.charAt(i))) {
        return false;
      }
    }

    return true;
  }

  /**
   * Returns {@code value} when it is non-null; otherwise returns {@code defaultValue}. Uses {@link
   * org.commonutils.lang.ObjectSupport#requireNonNullElse(Object, Object)}: if both arguments are
   * {@code null}, the result is {@code null}.
   *
   * @param value preferred string, may be {@code null}
   * @param defaultValue fallback when {@code value} is {@code null}; may be {@code null}
   * @return {@code value} if not {@code null}, else {@code defaultValue}
   * @see #defaultString(String)
   */
  public static @Nullable String defaultString(
      final @Nullable String value, final @Nullable String defaultValue) {
    return ObjectSupport.requireNonNullElse(value, defaultValue);
  }

  /**
   * Returns {@code defaultValue} when {@code value} is {@code null} or {@link String#isEmpty()};
   * otherwise returns {@code value} unchanged. A string of only spaces {@code " "} is <em>not</em>
   * empty—use {@link #defaultIfBlank(String, String)} if whitespace-only should trigger the
   * fallback.
   *
   * @param value input string, may be {@code null}
   * @param defaultValue replacement when {@code value} is null or empty
   * @return {@code value} if it has length &gt; 0, else {@code defaultValue}
   * @see #isEmpty(CharSequence)
   * @see #defaultIfBlank(String, String)
   */
  public static @Nullable String defaultIfEmpty(
      final @Nullable String value, final @Nullable String defaultValue) {
    return isEmpty(value) ? defaultValue : value;
  }

  /**
   * Returns {@code defaultValue} when {@code value} is {@code null}, empty, or {@link
   * #isBlank(CharSequence) blank}; otherwise returns {@code value} unchanged. Typical for optional
   * form fields and configuration strings where users may submit only spaces.
   *
   * @param value input string, may be {@code null}
   * @param defaultValue replacement when {@code value} is null, empty, or whitespace-only
   * @return {@code value} if it contains a non-whitespace character, else {@code defaultValue}
   * @see #isBlank(CharSequence)
   * @see #defaultIfEmpty(String, String)
   */
  public static @Nullable String defaultIfBlank(
      final @Nullable String value, final @Nullable String defaultValue) {
    return isBlank(value) ? defaultValue : value;
  }

  /**
   * Left-padding: repeats {@code paddingChar} {@code minLength - length()} times before the content
   * so that the result length is at least {@code minLength}. Matches JDK {@code
   * String.padStart(int,char)} semantics where that API exists (otherwise equivalent behavior built
   * from {@link String#repeat(int)} and {@link String#concat(String)}). A {@code null} {@code
   * value} is treated as {@code ""} before measuring length (see {@link #defaultString(String)}).
   *
   * @param value input string, may be {@code null} (treated as empty)
   * @param minLength desired minimum length; non-positive delta returns the coerced base string
   *     unchanged
   * @param paddingChar character to repeat on the left
   * @return padded string, never {@code null}
   * @see #padEnd(String, int, char)
   */
  public static @NonNull String padStart(
      final @Nullable String value, final int minLength, final char paddingChar) {
    final String base = defaultString(value);
    final int delta = minLength - base.length();
    if (delta <= 0) {
      return base;
    }
    return repeatPadding(paddingChar, delta).concat(base);
  }

  /**
   * Right-padding: repeats {@code paddingChar} after the content until the length is at least
   * {@code minLength}. Matches JDK {@code String.padEnd(int,char)} semantics where that API exists
   * (otherwise equivalent behavior built from {@link String#repeat(int)} and {@link
   * String#concat(String)}). A {@code null} {@code value} is treated as {@code ""} before measuring
   * length (see {@link #defaultString(String)}).
   *
   * @param value input string, may be {@code null} (treated as empty)
   * @param minLength desired minimum length; non-positive delta returns the coerced base string
   *     unchanged
   * @param paddingChar character to repeat on the right
   * @return padded string, never {@code null}
   * @see #padStart(String, int, char)
   */
  public static @NonNull String padEnd(
      final @Nullable String value, final int minLength, final char paddingChar) {
    final String base = defaultString(value);
    final int delta = minLength - base.length();
    if (delta <= 0) {
      return base;
    }
    return base.concat(repeatPadding(paddingChar, delta));
  }

  /** Repeats {@code paddingChar}; {@code repeatCount} must be at least {@code 1}. */
  private static String repeatPadding(final char paddingChar, final @Positive int repeatCount) {
    Contracts.requirePositive("repeatCount", repeatCount);
    return String.valueOf(paddingChar).repeat(repeatCount);
  }

  /**
   * Returns {@link String#trim()} applied to {@code value}, or {@code ""} when {@code value} is
   * {@code null}. Removes only leading and trailing characters with codepoint at most U+0020 (same
   * as {@link String#trim()}); does not remove e.g. ideographic space U+3000.
   *
   * @param value input string, may be {@code null}
   * @return trimmed string, never {@code null}
   * @see #stripToEmpty(String)
   */
  public static @NonNull String trimToEmpty(final @Nullable String value) {
    return defaultString(value).trim();
  }

  /**
   * Returns {@link String#trim()} applied to {@code value}, or {@code null} when {@code value} is
   * {@code null} or becomes empty after trimming. See {@link #trimToEmpty(String)} for which
   * characters are removed.
   *
   * @param value input string, may be {@code null}
   * @return trimmed string, or {@code null} if input was {@code null} or all trimmed away
   * @see #stripToNull(String)
   */
  public static @Nullable String trimToNull(final @Nullable String value) {
    if (ObjectSupport.isNull(value)) {
      return null;
    }

    final String trimmed = value.trim();
    return trimmed.isEmpty() ? null : trimmed;
  }

  /**
   * Returns {@link String#strip()} applied to {@code value}, or {@code ""} when {@code value} is
   * {@code null}. Removes Unicode leading and trailing whitespace (including e.g. U+3000
   * ideographic space), per {@link String#strip()}.
   *
   * @param value input string, may be {@code null}
   * @return stripped string, never {@code null}
   * @see #trimToEmpty(String)
   */
  public static @NonNull String stripToEmpty(final @Nullable String value) {
    return defaultString(value).strip();
  }

  /**
   * Returns {@link String#strip()} applied to {@code value}, or {@code null} when {@code value} is
   * {@code null} or becomes empty after stripping. See {@link #stripToEmpty(String)} for which
   * characters are removed.
   *
   * @param value input string, may be {@code null}
   * @return stripped string, or {@code null} if input was {@code null} or all stripped away
   * @see #trimToNull(String)
   */
  public static @Nullable String stripToNull(final @Nullable String value) {
    if (ObjectSupport.isNull(value)) {
      return null;
    }

    final String stripped = value.strip();
    return stripped.isEmpty() ? null : stripped;
  }

  /**
   * Per-character case-insensitive match used by {@link #equalsIgnoreCase(CharSequence,
   * CharSequence)}.
   */
  private static boolean charsEqualIgnoreCase(final char left, final char right) {
    return Character.toUpperCase(left) == Character.toUpperCase(right)
        || Character.toLowerCase(left) == Character.toLowerCase(right);
  }
}
