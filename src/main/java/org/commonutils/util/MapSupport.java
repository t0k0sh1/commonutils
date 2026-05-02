package org.commonutils.util;

import java.util.Map;
import org.commonutils.annotation.NonNegative;
import org.commonutils.annotation.NonNull;
import org.commonutils.annotation.Nullable;
import org.commonutils.internal.Contracts;
import org.commonutils.lang.NumberSupport;
import org.commonutils.lang.ObjectSupport;

/**
 * Static helpers for {@link Map} work: null-safe emptiness checks, default substitution, and typed
 * reads with fallbacks. Use this class for configuration maps, loosely typed JSON-style objects,
 * and other {@code Map<?, ?>} sources where repeating null and {@code containsKey} guards is noisy.
 *
 * <h2 id="null-handling">Null handling</h2>
 *
 * Unless stated otherwise, a {@code null} map reference is treated as &quot;missing&quot;:
 *
 * <ul>
 *   <li>Predicates such as {@link #isEmpty(Map)} treat {@code null} like an empty map.
 *   <li>{@link #size(Map)} returns {@code 0} when {@code map} is {@code null}; otherwise {@link
 *       Map#size()}.
 *   <li>Accessor methods return the supplied default when the map is {@code null}, the key is not
 *       contained, parsing fails, or (for some methods) the stored value is {@code null}; see each
 *       method for exact rules.
 * </ul>
 *
 * This matches {@link CollectionSupport} and {@link org.commonutils.lang.StringSupport} conventions
 * (and differs from calling {@link Map} instance methods on {@code null}, which would throw {@link
 * NullPointerException}).
 *
 * <h2 id="keys-vs-values">Absent keys vs {@code null} values</h2>
 *
 * Accessors use {@link Map#containsKey(Object)} before reading values. A missing key and a key
 * mapped to {@code null} can therefore behave differently depending on the method: for example,
 * {@link #getString(Map, Object, String)} uses {@link ObjectSupport#toString(Object, String)} on
 * the stored value, so an explicit {@code null} value yields the string default, whereas {@link
 * #getBoolean(Map, Object, boolean)} treats a {@code null} value like &quot;no usable boolean&quot;
 * and returns the boolean default. Refer to each method when the distinction matters.
 *
 * <h2 id="typed-getters">Typed getters</h2>
 *
 * {@link #getInt(Map, Object, int)} and {@link #getLong(Map, Object, long)} accept {@link Number}
 * instances in the map (using {@link Number#intValue()} / {@link Number#longValue()}). For other
 * types the value is converted with {@link ObjectSupport#toString(Object, String)} and parsed via
 * {@link NumberSupport#toInt(String, int)} / {@link NumberSupport#toLong(String, long)} (trimming
 * and {@link NumberFormatException} handling per those methods).
 *
 * <h2 id="booleans">Boolean interpretation</h2>
 *
 * {@link #getBoolean(Map, Object, boolean)} unwraps {@link Boolean} directly. Other types are
 * converted to a string and parsed with {@link Boolean#parseBoolean(String)} only (typically only
 * the literal {@code "true"}, ignoring case, is {@code true}). Values such as {@code "yes"} or
 * {@code "1"} are not treated as {@code true}; interpret those strings at the call site if your
 * configuration uses alternate spellings.
 *
 * <h2 id="exceptions">Exceptions</h2>
 *
 * These helpers do <em>not</em> throw for ordinary null maps, missing keys, or failed parsing of
 * string values: they return defaults as documented. {@link #size(Map)} throws {@link
 * IllegalArgumentException} when {@link Map#size()} returns a negative value. {@link
 * #defaultIfNull(Map, Map)} delegates to {@link ObjectSupport#requireNonNullElse(Object, Object)}
 * and throws {@link NullPointerException} if both arguments are {@code null}. A broken {@link Map}
 * implementation in user code may still throw at runtime.
 *
 * <h2 id="examples">Examples</h2>
 *
 * <pre>{@code
 * if (MapSupport.isEmpty(settings)) {
 *     return Defaults.CONFIG;
 * }
 *
 * int port = MapSupport.getInt(settings, "port", 8080);
 * boolean ssl = MapSupport.getBoolean(settings, "ssl");
 * String name = MapSupport.getString(settings, "name", "anonymous");
 * }</pre>
 *
 * <p><strong id="nullability">Nullability annotations</strong> ({@link
 * org.commonutils.annotation.Nullable}, {@link org.commonutils.annotation.NonNull}) document
 * contracts on signatures; {@link #defaultIfNull(Map, Map)} follows {@link
 * ObjectSupport#requireNonNullElse(Object, Object)} (both maps {@code null} yields {@link
 * NullPointerException}).
 *
 * <p><strong id="numeric-contracts">Numeric constraints</strong> ({@link
 * org.commonutils.annotation.NonNegative}, {@link org.commonutils.annotation.Positive}) document
 * ranges where applicable in related APIs; {@link #size(Map)} enforces {@link
 * org.commonutils.annotation.NonNegative} at runtime when {@link Map#size()} returns a negative
 * value. Other map accessors use defaults rather than validating numeric ranges on keys.
 *
 * @see Map#isEmpty()
 * @see Map#size()
 * @see Map#containsKey(Object)
 * @see CollectionSupport
 */
public final class MapSupport {
  private MapSupport() {}

  /**
   * Returns {@code true} if {@code map} is {@code null} or {@link Map#isEmpty()}.
   *
   * @param map map to test, may be {@code null}
   * @return {@code true} if {@code map} is {@code null} or has no entries
   * @see #isNotEmpty(Map)
   * @see CollectionSupport#isEmpty(java.util.Collection)
   */
  public static boolean isEmpty(final @Nullable Map<?, ?> map) {
    return ObjectSupport.isNull(map) || map.isEmpty();
  }

  /**
   * Negation of {@link #isEmpty(Map)}: {@code true} when {@code map} is non-null and has at least
   * one entry.
   *
   * @param map map to test, may be {@code null}
   * @return {@code true} if {@code map} is not {@code null} and not empty
   * @see #isEmpty(Map)
   */
  public static boolean isNotEmpty(final @Nullable Map<?, ?> map) {
    return !isEmpty(map);
  }

  /**
   * Returns {@code map.size()} when {@code map} is non-null; otherwise {@code 0}. Use instead of
   * {@code map == null ? 0 : map.size()} at call sites.
   *
   * @param map map to measure, may be {@code null}
   * @return entry count, or {@code 0} when {@code map} is {@code null}
   * @throws IllegalArgumentException when {@code map} is non-null and {@link Map#size()} is
   *     negative
   * @see #isEmpty(Map)
   * @see CollectionSupport#size(java.util.Collection)
   */
  public static @NonNegative int size(final @Nullable Map<?, ?> map) {
    if (ObjectSupport.isNull(map)) {
      return 0;
    }
    final int n = map.size();
    Contracts.requireNonNegative("map.size()", n);
    return n;
  }

  /**
   * Returns {@code map} when it is non-null; otherwise returns {@code defaultMap}. Equivalent to
   * {@link ObjectSupport#requireNonNullElse(Object, Object)}.
   *
   * <p>If both {@code map} and {@code defaultMap} are {@code null}, throws {@link
   * NullPointerException}.
   *
   * @param map primary map, may be {@code null}
   * @param defaultMap fallback when {@code map} is {@code null}; must be non-null if {@code map}
   *     may be {@code null} and callers need a non-null result
   * @param <T> concrete map type
   * @return {@code map} if non-null, otherwise {@code defaultMap}
   * @see ObjectSupport#requireNonNullElse(Object, Object)
   */
  public static <T extends Map<?, ?>> @NonNull T defaultIfNull(
      final @Nullable T map, final @Nullable T defaultMap) {
    return ObjectSupport.requireNonNullElse(map, defaultMap);
  }

  /**
   * Returns the string form of the value mapped to {@code key}, or {@code null} when {@code map} is
   * {@code null}, the key is absent, or the stored value is {@code null} (via {@link
   * ObjectSupport#toString(Object, String)} with a {@code null} default).
   *
   * @param map map to read from, may be {@code null}
   * @param key key whose value to stringify, may be {@code null} if the map allows it
   * @return string value, or {@code null} when falling back as described
   * @see #getString(Map, Object, String)
   */
  public static @Nullable String getString(
      final @Nullable Map<?, ?> map, final @Nullable Object key) {
    return getString(map, key, null);
  }

  /**
   * Returns the string form of the value mapped to {@code key}, or {@code defaultValue} when {@code
   * map} is {@code null} or the key is absent. When the key is present, returns {@link
   * ObjectSupport#toString(Object, String)} with {@code defaultValue} as the null default (so a
   * stored {@code null} yields {@code defaultValue}).
   *
   * @param map map to read from, may be {@code null}
   * @param key key whose value to stringify, may be {@code null} if the map allows it
   * @param defaultValue value when the map is null, the key is missing, or the value stringifies to
   *     {@code null}
   * @return string value or {@code defaultValue}
   * @see #getString(Map, Object)
   */
  public static @Nullable String getString(
      final @Nullable Map<?, ?> map,
      final @Nullable Object key,
      final @Nullable String defaultValue) {
    if (ObjectSupport.isNull(map) || !map.containsKey(key)) {
      return defaultValue;
    }

    return ObjectSupport.toString(map.get(key), defaultValue);
  }

  /**
   * Returns the {@code int} mapped to {@code key}, or {@code 0} when the map is {@code null}, the
   * key is absent, the value is {@code null}, or parsing fails.
   *
   * @param map map to read from, may be {@code null}
   * @param key key whose value to interpret
   * @return parsed {@code int}, or {@code 0} when falling back
   * @see #getInt(Map, Object, int)
   */
  public static int getInt(final @Nullable Map<?, ?> map, final @Nullable Object key) {
    return getInt(map, key, 0);
  }

  /**
   * Returns the {@code int} mapped to {@code key}, or {@code defaultValue} when {@code map} is
   * {@code null}, the key is absent, or the value cannot be parsed as an {@code int}. If the value
   * is a {@link Number}, returns {@link Number#intValue()}; otherwise the value is converted with
   * {@link ObjectSupport#toString(Object, String)} and passed to {@link NumberSupport#toInt(String,
   * int)}.
   *
   * @param map map to read from, may be {@code null}
   * @param key key whose value to interpret
   * @param defaultValue value when the map is null, the key is missing, the value is null, or
   *     parsing fails
   * @return parsed {@code int} or {@code defaultValue}
   * @see #getLong(Map, Object, long)
   */
  public static int getInt(
      final @Nullable Map<?, ?> map, final @Nullable Object key, final int defaultValue) {
    if (ObjectSupport.isNull(map) || !map.containsKey(key)) {
      return defaultValue;
    }

    final Object value = map.get(key);
    if (value instanceof Number number) {
      return number.intValue();
    }

    return NumberSupport.toInt(ObjectSupport.toString(value, null), defaultValue);
  }

  /**
   * Returns the {@code long} mapped to {@code key}, or {@code 0L} when the map is {@code null}, the
   * key is absent, the value is {@code null}, or parsing fails.
   *
   * @param map map to read from, may be {@code null}
   * @param key key whose value to interpret
   * @return parsed {@code long}, or {@code 0L} when falling back
   * @see #getLong(Map, Object, long)
   */
  public static long getLong(final @Nullable Map<?, ?> map, final @Nullable Object key) {
    return getLong(map, key, 0L);
  }

  /**
   * Returns the {@code long} mapped to {@code key}, or {@code defaultValue} when {@code map} is
   * {@code null}, the key is absent, or the value cannot be parsed as a {@code long}. If the value
   * is a {@link Number}, returns {@link Number#longValue()}; otherwise the value is converted with
   * {@link ObjectSupport#toString(Object, String)} and passed to {@link
   * NumberSupport#toLong(String, long)}.
   *
   * @param map map to read from, may be {@code null}
   * @param key key whose value to interpret
   * @param defaultValue value when the map is null, the key is missing, the value is null, or
   *     parsing fails
   * @return parsed {@code long} or {@code defaultValue}
   * @see #getInt(Map, Object, int)
   */
  public static long getLong(
      final @Nullable Map<?, ?> map, final @Nullable Object key, final long defaultValue) {
    if (ObjectSupport.isNull(map) || !map.containsKey(key)) {
      return defaultValue;
    }

    final Object value = map.get(key);
    if (value instanceof Number number) {
      return number.longValue();
    }

    return NumberSupport.toLong(ObjectSupport.toString(value, null), defaultValue);
  }

  /**
   * Returns the boolean value mapped to {@code key}, or {@code false} when the map is {@code null},
   * the key is absent, the stored value is {@code null}, or a non-boolean value does not parse as
   * {@code true} per {@link Boolean#parseBoolean(String)}.
   *
   * @param map map to read from, may be {@code null}
   * @param key key whose value to interpret
   * @return parsed boolean, or {@code false} when falling back
   * @see #getBoolean(Map, Object, boolean)
   */
  public static boolean getBoolean(final @Nullable Map<?, ?> map, final @Nullable Object key) {
    return getBoolean(map, key, false);
  }

  /**
   * Returns the boolean value mapped to {@code key}, or {@code defaultValue} when the map is {@code
   * null}, the key is absent, or the stored value is {@code null}. When the value is a {@link
   * Boolean}, it is unboxed. For any other type, {@link ObjectSupport#toString(Object, String)} is
   * used with a {@code null} second argument; if that yields {@code null}, {@code defaultValue} is
   * returned. Otherwise the result is {@link Boolean#parseBoolean(String)} on that string (same
   * rule as the JDK: only {@code "true"}, ignoring case, yields {@code true}). Alternate string
   * conventions are not supported; read the value as a string (for example via {@link
   * #getString(Map, Object, String)}) and parse it with application-specific rules when needed.
   *
   * @param map map to read from, may be {@code null}
   * @param key key whose value to interpret
   * @param defaultValue value when the key is missing, the map is null, the value is null, or
   *     string conversion yields null
   * @return interpreted boolean
   */
  public static boolean getBoolean(
      final @Nullable Map<?, ?> map, final @Nullable Object key, final boolean defaultValue) {
    if (ObjectSupport.isNull(map) || !map.containsKey(key)) {
      return defaultValue;
    }

    final Object value = map.get(key);
    if (value instanceof Boolean booleanValue) {
      return booleanValue;
    }

    final String text = ObjectSupport.toString(value, null);
    if (ObjectSupport.isNull(text)) {
      return defaultValue;
    }

    return Boolean.parseBoolean(text);
  }
}
