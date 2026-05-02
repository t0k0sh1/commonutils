package org.commonutils.lang;

import java.util.Comparator;
import java.util.function.Supplier;
import org.commonutils.annotation.NonNegative;
import org.commonutils.annotation.NonNull;
import org.commonutils.annotation.Nullable;
import org.commonutils.internal.Contracts;

/**
 * Static helpers aligned with {@link java.util.Objects}: almost every method forwards to the JDK so
 * semantics stay identical to the platform. Use this class when you want the same behavior as
 * {@code Objects} but prefer a single import namespace alongside {@link
 * org.commonutils.lang.StringSupport} and friends, or when you need extensions the JDK does not
 * provide.
 *
 * <h2>Extensions beyond {@link java.util.Objects}</h2>
 *
 * <ul>
 *   <li>{@link #firstNonNull(Object...)} / {@link #lastNonNull(Object...)} &mdash; pick the first
 *       or last non-null from a varargs list
 *   <li>{@link #toStringElseGet(Object, Supplier)} &mdash; like {@link
 *       java.util.Objects#toString(Object, String)} but the fallback is supplied lazily
 *   <li>{@link #requireNonNull(Object, Object...)} &mdash; validate several references in one call
 * </ul>
 *
 * All other methods are direct forwards; see the linked {@code Objects} API for full behavior.
 *
 * <p><strong id="nullability">Nullability annotations</strong> ({@link
 * org.commonutils.annotation.Nullable}, {@link org.commonutils.annotation.NonNull}) document
 * contracts on signatures; {@link org.commonutils.annotation.NonNull}-annotated reference
 * parameters are validated at runtime with {@link java.lang.NullPointerException} where applicable.
 *
 * <p><strong id="numeric-contracts">Numeric constraints</strong> ({@link
 * org.commonutils.annotation.NonNegative}, {@link org.commonutils.annotation.Positive}) document
 * ranges on signatures; implementations enforce them at runtime with {@link
 * IllegalArgumentException}.
 *
 * @see java.util.Objects
 */
public final class ObjectSupport {
  private ObjectSupport() {}

  /**
   * Null-safe equality; same result as {@link java.util.Objects#equals(Object, Object)}.
   *
   * @param a first value, may be {@code null}
   * @param b second value, may be {@code null}
   * @return {@code true} if both references are {@code null}, or if both are non-null and {@link
   *     Object#equals(Object) Object.equals} would return {@code true}
   * @see java.util.Objects#equals(Object, Object)
   */
  public static boolean equals(final @Nullable Object a, final @Nullable Object b) {
    return java.util.Objects.equals(a, b);
  }

  /**
   * Equality for arrays and nested structures; same as {@link java.util.Objects#deepEquals(Object,
   * Object)}.
   *
   * @param a first value, may be {@code null}
   * @param b second value, may be {@code null}
   * @return {@code true} per deep equality rules of {@code Objects}
   * @see java.util.Objects#deepEquals(Object, Object)
   */
  public static boolean deepEquals(final @Nullable Object a, final @Nullable Object b) {
    return java.util.Objects.deepEquals(a, b);
  }

  /**
   * Hash code for {@code null} or delegate; same as {@link java.util.Objects#hashCode(Object)}.
   *
   * @param o value, may be {@code null}
   * @return {@code 0} for {@code null}, else {@code o.hashCode()}
   * @see java.util.Objects#hashCode(Object)
   */
  public static int hashCode(final @Nullable Object o) {
    return java.util.Objects.hashCode(o);
  }

  /**
   * @see java.util.Objects#hash(Object...)
   */
  public static int hash(final Object @Nullable ... values) {
    return java.util.Objects.hash(values);
  }

  /**
   * @see java.util.Objects#toString(Object)
   */
  public static @Nullable String toString(final @Nullable Object o) {
    return java.util.Objects.toString(o);
  }

  /**
   * @see java.util.Objects#toString(Object, String)
   */
  public static @Nullable String toString(
      final @Nullable Object o, final @Nullable String nullDefault) {
    return java.util.Objects.toString(o, nullDefault);
  }

  /**
   * Like {@link java.util.Objects#toString(Object, String)} but obtains the fallback only when
   * {@code o} is {@code null}, via {@code defaultSupplier}.
   *
   * @param defaultSupplier invoked only when {@code o} is {@code null}; must not be {@code null}
   * @return {@code o.toString()} when {@code o} is non-null; otherwise {@code
   *     defaultSupplier.get()}
   */
  public static @Nullable String toStringElseGet(
      final @Nullable Object o, final @NonNull Supplier<@Nullable String> defaultSupplier) {
    java.util.Objects.requireNonNull(defaultSupplier, "defaultSupplier");
    if (o != null) {
      return o.toString();
    }
    return defaultSupplier.get();
  }

  /**
   * @see java.util.Objects#toIdentityString(Object)
   */
  public static @NonNull String toIdentityString(final @NonNull Object o) {
    java.util.Objects.requireNonNull(o, "o");
    return java.util.Objects.toIdentityString(o);
  }

  /**
   * @see java.util.Objects#compare(Object, Object, Comparator)
   */
  public static <T> int compare(
      final @Nullable T a, final @Nullable T b, final @NonNull Comparator<? super T> c) {
    java.util.Objects.requireNonNull(c, "c");
    return java.util.Objects.compare(a, b, c);
  }

  /**
   * Returns {@code obj} or throws; same as {@link java.util.Objects#requireNonNull(Object)}.
   *
   * @param obj reference that must not be {@code null}
   * @param <T> value type
   * @return {@code obj}
   * @throws NullPointerException if {@code obj} is {@code null}
   * @see java.util.Objects#requireNonNull(Object)
   */
  public static <T> @NonNull T requireNonNull(final @Nullable T obj) {
    return java.util.Objects.requireNonNull(obj);
  }

  /**
   * @see java.util.Objects#requireNonNull(Object, String)
   */
  public static <T> @NonNull T requireNonNull(
      final @Nullable T obj, final @Nullable String message) {
    return java.util.Objects.requireNonNull(obj, message);
  }

  /**
   * @see java.util.Objects#requireNonNull(Object, Supplier)
   */
  public static <T> @NonNull T requireNonNull(
      final @Nullable T obj, final @NonNull Supplier<String> messageSupplier) {
    java.util.Objects.requireNonNull(messageSupplier, "messageSupplier");
    return java.util.Objects.requireNonNull(obj, messageSupplier);
  }

  /**
   * Ensures every argument is non-null. Equivalent to invoking {@link
   * java.util.Objects#requireNonNull(Object)} on {@code first} and each element of {@code rest}.
   *
   * <p>Prefer passing extra references as separate arguments ({@code requireNonNull(a, b, c)})
   * rather than a single {@code null}-filled array ({@code requireNonNull(a, new Object[]
   * {null})}), except when testing. A bare {@code null} as the second argument is ambiguous with
   * {@link #requireNonNull(Object, String)}: {@code requireNonNull(x, (String) null)} resolves to
   * the message overload, which does not throw when {@code x} is non-null.
   *
   * @throws NullPointerException if {@code first}, the {@code rest} array itself, or any element of
   *     {@code rest} is {@code null}
   */
  public static void requireNonNull(final @Nullable Object first, final Object @Nullable ... rest) {
    java.util.Objects.requireNonNull(first);
    java.util.Objects.requireNonNull(rest, "rest");
    for (final Object o : rest) {
      java.util.Objects.requireNonNull(o);
    }
  }

  /**
   * Returns the leftmost non-null argument.
   *
   * @param values references scanned from index {@code 0} upward; a {@code null} varargs array
   *     yields {@code null}
   * @return the first non-null value, or {@code null} if every argument is {@code null}
   */
  @SafeVarargs
  public static <T> @Nullable T firstNonNull(final T @Nullable ... values) {
    if (values != null) {
      for (final T value : values) {
        if (value != null) {
          return value;
        }
      }
    }
    return null;
  }

  /**
   * Returns the rightmost non-null argument (last index wins among candidates).
   *
   * @param values scanned from the last index downward; a {@code null} varargs array yields {@code
   *     null}
   * @return that non-null value, or {@code null} if every argument is {@code null}
   */
  @SafeVarargs
  public static <T> @Nullable T lastNonNull(final T @Nullable ... values) {
    if (values != null) {
      for (int i = values.length - 1; i >= 0; i--) {
        final T value = values[i];
        if (value != null) {
          return value;
        }
      }
    }
    return null;
  }

  /**
   * {@code true} when {@code obj == null}; same as {@link java.util.Objects#isNull(Object)}.
   *
   * @param obj reference to test, may be {@code null}
   * @return {@code true} if {@code obj} is {@code null}
   * @see java.util.Objects#isNull(Object)
   */
  public static boolean isNull(final @Nullable Object obj) {
    return java.util.Objects.isNull(obj);
  }

  /**
   * {@code true} when {@code obj != null}; same as {@link java.util.Objects#nonNull(Object)}.
   *
   * @param obj reference to test, may be {@code null}
   * @return {@code true} if {@code obj} is not {@code null}
   * @see java.util.Objects#nonNull(Object)
   */
  public static boolean nonNull(final @Nullable Object obj) {
    return java.util.Objects.nonNull(obj);
  }

  /**
   * @see java.util.Objects#requireNonNullElse(Object, Object)
   */
  public static <T> @NonNull T requireNonNullElse(
      final @Nullable T obj, final @Nullable T defaultObj) {
    return java.util.Objects.requireNonNullElse(obj, defaultObj);
  }

  /**
   * @see java.util.Objects#requireNonNullElseGet(Object, Supplier)
   */
  public static <T> @NonNull T requireNonNullElseGet(
      final @Nullable T obj, final @NonNull Supplier<? extends T> supplier) {
    java.util.Objects.requireNonNull(supplier, "supplier");
    return java.util.Objects.requireNonNullElseGet(obj, supplier);
  }

  /**
   * @see java.util.Objects#checkIndex(int, int)
   */
  public static int checkIndex(final int index, final @NonNegative int length) {
    Contracts.requireNonNegative("length", length);
    return java.util.Objects.checkIndex(index, length);
  }

  /**
   * @see java.util.Objects#checkFromToIndex(int, int, int)
   */
  public static int checkFromToIndex(
      final int fromIndex, final int toIndex, final @NonNegative int length) {
    Contracts.requireNonNegative("length", length);
    return java.util.Objects.checkFromToIndex(fromIndex, toIndex, length);
  }

  /**
   * @see java.util.Objects#checkFromIndexSize(int, int, int)
   */
  public static int checkFromIndexSize(
      final int fromIndex, final @NonNegative int size, final @NonNegative int length) {
    Contracts.requireNonNegative("size", size);
    Contracts.requireNonNegative("length", length);
    return java.util.Objects.checkFromIndexSize(fromIndex, size, length);
  }

  /**
   * @see java.util.Objects#checkIndex(long, long)
   */
  public static long checkIndex(final long index, final @NonNegative long length) {
    Contracts.requireNonNegative("length", length);
    return java.util.Objects.checkIndex(index, length);
  }

  /**
   * @see java.util.Objects#checkFromToIndex(long, long, long)
   */
  public static long checkFromToIndex(
      final long fromIndex, final long toIndex, final @NonNegative long length) {
    Contracts.requireNonNegative("length", length);
    return java.util.Objects.checkFromToIndex(fromIndex, toIndex, length);
  }

  /**
   * @see java.util.Objects#checkFromIndexSize(long, long, long)
   */
  public static long checkFromIndexSize(
      final long fromIndex, final @NonNegative long size, final @NonNegative long length) {
    Contracts.requireNonNegative("size", size);
    Contracts.requireNonNegative("length", length);
    return java.util.Objects.checkFromIndexSize(fromIndex, size, length);
  }
}
