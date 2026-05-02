package org.commonutils.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a type usage whose value must not be {@code null}. For documentation and static analysis;
 * on public API methods in this library that validate arguments, passing {@code null} where {@code
 * NonNull} is required causes {@link NullPointerException} (see each method's description).
 *
 * @see org.commonutils.annotation.Nullable
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
public @interface NonNull {}
