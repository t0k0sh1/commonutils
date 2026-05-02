package org.commonutils.util;

import java.util.Collection;
import java.util.Iterator;
import java.util.SequencedCollection;
import org.commonutils.annotation.NonNegative;
import org.commonutils.annotation.Nullable;
import org.commonutils.internal.Contracts;
import org.commonutils.lang.ObjectSupport;

/**
 * Static helpers shared by arbitrary {@link Collection} implementations ({@link java.util.List},
 * {@link java.util.Set}, {@link java.util.Queue}, …): presence checks, safe {@link
 * #size(Collection)}, containment tests between collections, and ordered endpoints via {@link
 * SequencedCollection} where applicable.
 *
 * <h2 id="null-handling">Null handling</h2>
 *
 * Unless stated otherwise, a {@code null} collection reference is treated as &quot;missing&quot;:
 *
 * <ul>
 *   <li>Predicates such as {@link #isEmpty(Collection)} treat {@code null} like an empty
 *       collection.
 *   <li>Methods that return an element or a count map {@code null} or empty inputs to {@code null}
 *       or {@code 0}; see each method for exact rules.
 * </ul>
 *
 * This matches {@link MapSupport} and {@link org.commonutils.lang.StringSupport} conventions (and
 * differs from calling {@link Collection} instance methods on {@code null}, which would throw
 * {@link NullPointerException}).
 *
 * <h2 id="exceptions">Exceptions</h2>
 *
 * These helpers do <em>not</em> throw for ordinary null or empty arguments: they return {@code
 * false}, {@code 0}, or {@code null} as documented. They are intended for guard-friendly call
 * sites. Implementations still delegate to the supplied collections; a broken {@link Iterator} or
 * other contract violation in user code may still throw at runtime. {@link #size(Collection)}
 * throws {@link IllegalArgumentException} when {@link Collection#size()} returns a negative value.
 *
 * <h2 id="containment">Containment helpers</h2>
 *
 * {@link #containsAny(Collection, Collection)} and {@link #containsAll(Collection, Collection)}
 * answer membership questions between two collections. {@link #containsAll(Collection, Collection)}
 * is null-safe wrapper around {@link Collection#containsAll(Collection)}; when either argument is
 * {@code null}, the result is {@code false}. An empty {@code candidates} collection yields {@code
 * true} for {@code containsAll} (every required element is vacuously present), and {@code false}
 * for {@code containsAny} (nothing to match).
 *
 * <h2 id="first-last">First and last elements</h2>
 *
 * {@link #getFirst(Collection)} prefers {@link SequencedCollection#getFirst()} when the argument is
 * a {@link SequencedCollection}; otherwise it uses the first element from an {@link Iterator}.
 * {@link #getLast(SequencedCollection)} uses {@link SequencedCollection#getLast()} and applies to
 * ordered collections such as lists, deques, and {@link java.util.LinkedHashSet}.
 *
 * <h2 id="examples">Examples</h2>
 *
 * <pre>{@code
 * // Safe sizing for possibly-uninitialized field
 * int n = CollectionSupport.size(maybeIds);
 *
 * // Membership without null checks
 * if (CollectionSupport.containsAny(granted, required)) {
 *     // at least one required permission is granted
 * }
 *
 * if (CollectionSupport.isEmpty(rows)) {
 *     return List.of();
 * }
 * }</pre>
 *
 * <p><strong id="nullability">Nullability annotations</strong> ({@link
 * org.commonutils.annotation.Nullable}, {@link org.commonutils.annotation.NonNull}) document
 * contracts on signatures.
 *
 * <p><strong id="numeric-contracts">Numeric constraints</strong> ({@link
 * org.commonutils.annotation.NonNegative}, {@link org.commonutils.annotation.Positive}) document
 * intended primitive ranges for callers; {@link #size(Collection)} enforces {@link
 * org.commonutils.annotation.NonNegative} at runtime when {@link Collection#size()} returns a
 * negative value.
 *
 * @see Collection#isEmpty()
 * @see Collection#containsAll(Collection)
 * @see SequencedCollection#getFirst()
 * @see SequencedCollection#getLast()
 */
public final class CollectionSupport {
  private CollectionSupport() {}

  /**
   * Returns {@code true} if {@code collection} is {@code null} or {@link Collection#isEmpty()}.
   *
   * @param collection collection to test, may be {@code null}
   * @return {@code true} if {@code collection} is {@code null} or has no elements
   * @see #isNotEmpty(Collection)
   * @see #size(Collection)
   */
  public static boolean isEmpty(final @Nullable Collection<?> collection) {
    return ObjectSupport.isNull(collection) || collection.isEmpty();
  }

  /**
   * Negation of {@link #isEmpty(Collection)}: {@code true} when {@code collection} is non-null and
   * has at least one element.
   *
   * @param collection collection to test, may be {@code null}
   * @return {@code true} if {@code collection} is not {@code null} and not empty
   * @see #isEmpty(Collection)
   */
  public static boolean isNotEmpty(final @Nullable Collection<?> collection) {
    return !isEmpty(collection);
  }

  /**
   * Returns {@code collection.size()} when {@code collection} is non-null; otherwise {@code 0}. Use
   * instead of {@code collection == null ? 0 : collection.size()} at call sites.
   *
   * @param collection collection to measure, may be {@code null}
   * @return element count, or {@code 0} when {@code collection} is {@code null}
   * @throws IllegalArgumentException when {@code collection} is non-null and {@link
   *     Collection#size()} is negative
   * @see #isEmpty(Collection)
   */
  public static @NonNegative int size(final @Nullable Collection<?> collection) {
    if (ObjectSupport.isNull(collection)) {
      return 0;
    }
    final int n = collection.size();
    Contracts.requireNonNegative("collection.size()", n);
    return n;
  }

  /**
   * Returns the first element of {@code collection}, or {@code null} when {@code collection} is
   * {@code null} or has no elements.
   *
   * <p>When the first stored element is {@code null} (allowed by some collection implementations),
   * this method also returns {@code null}; callers cannot distinguish that case from an empty
   * collection using only the return value.
   *
   * @param collection collection to read from, may be {@code null}
   * @param <T> element type
   * @return first element, or {@code null} if missing
   * @see #getLast(SequencedCollection)
   */
  public static <T> @Nullable T getFirst(final @Nullable Collection<? extends T> collection) {
    if (ObjectSupport.isNull(collection) || collection.isEmpty()) {
      return null;
    }
    if (collection instanceof SequencedCollection<? extends T> sequenced) {
      return sequenced.getFirst();
    }
    final Iterator<? extends T> iterator = collection.iterator();
    return iterator.hasNext() ? iterator.next() : null;
  }

  /**
   * Returns the last element of {@code collection}, or {@code null} when {@code collection} is
   * {@code null} or empty.
   *
   * <p>When the last stored element is {@code null}, this method also returns {@code null}; callers
   * cannot distinguish that case from an empty collection using only the return value.
   *
   * @param collection sequenced collection to read from, may be {@code null}
   * @param <T> element type
   * @return last element, or {@code null} if missing
   * @see #getFirst(Collection)
   */
  public static <T> @Nullable T getLast(
      final @Nullable SequencedCollection<? extends T> collection) {
    if (ObjectSupport.isNull(collection) || collection.isEmpty()) {
      return null;
    }
    return collection.getLast();
  }

  /**
   * Returns {@code true} if {@code source} contains at least one element that appears in {@code
   * candidates}, using {@link Collection#contains(Object)} semantics (equality per the source
   * collection).
   *
   * <p>When either collection is {@code null} or empty, or {@code candidates} is empty, the result
   * is {@code false}.
   *
   * @param source collection to search in, may be {@code null}
   * @param candidates values to look for, may be {@code null}
   * @return {@code true} if at least one candidate is contained in {@code source}
   * @see #containsAll(Collection, Collection)
   */
  public static boolean containsAny(
      final @Nullable Collection<?> source, final @Nullable Collection<?> candidates) {
    if (isEmpty(source) || isEmpty(candidates)) {
      return false;
    }

    for (final Object candidate : candidates) {
      if (source.contains(candidate)) {
        return true;
      }
    }

    return false;
  }

  /**
   * Returns {@code true} if {@code source} contains every element of {@code candidates}, using
   * {@link Collection#containsAll(Collection)}. When {@code source} or {@code candidates} is {@code
   * null}, returns {@code false}. When {@code candidates} is empty, returns {@code true} (vacuous
   * truth).
   *
   * @param source collection to search in, may be {@code null}
   * @param candidates required elements, may be {@code null}
   * @return {@code true} if every candidate is contained in {@code source}
   * @see #containsAny(Collection, Collection)
   * @see Collection#containsAll(Collection)
   */
  public static boolean containsAll(
      final @Nullable Collection<?> source, final @Nullable Collection<?> candidates) {
    if (ObjectSupport.isNull(source) || ObjectSupport.isNull(candidates)) {
      return false;
    }

    return source.containsAll(candidates);
  }
}
