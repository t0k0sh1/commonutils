package org.commonutils.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a numeric type usage intended to be zero or greater: primitive integrals ({@code int},
 * {@code long}, …) — {@literal >= 0}; non-{@code NaN} floating-point — {@literal >= 0.0}. For
 * documentation and static analysis. On public API methods in this library that validate arguments,
 * out-of-range values cause {@link IllegalArgumentException} (see each method).
 *
 * <p>This annotation does not add runtime range checks to your own API; use explicit validation or
 * tooling (see {@link org.commonutils.annotation the package description}).
 *
 * @see org.commonutils.annotation.Positive
 * @see org.commonutils.annotation.NonNull
 * @see org.commonutils.annotation.Nullable
 */
@Documented
@Retention(RetentionPolicy.CLASS)
@Target({
  ElementType.TYPE_USE,
  ElementType.FIELD,
  ElementType.METHOD,
  ElementType.PARAMETER,
  ElementType.CONSTRUCTOR,
  ElementType.TYPE_PARAMETER,
  ElementType.LOCAL_VARIABLE,
})
public @interface NonNegative {}
