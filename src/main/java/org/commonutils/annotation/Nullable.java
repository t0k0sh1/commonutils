package org.commonutils.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a type usage whose value may be {@code null}. Callers should read each method for how
 * {@code null} is interpreted (for example treated as empty, or rejected after validation). Where a
 * parameter is also validated as non-null at runtime, the method uses {@link NullPointerException}
 * for violations rather than relying on this marker alone.
 *
 * <p>This is a documentation marker only on your own declarations; it does not cause commonutils to
 * validate your code at runtime (see {@link org.commonutils.annotation the package description}).
 *
 * @see org.commonutils.annotation.NonNull
 * @see org.commonutils.annotation.NonNegative
 * @see org.commonutils.annotation.Positive
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
public @interface Nullable {}
