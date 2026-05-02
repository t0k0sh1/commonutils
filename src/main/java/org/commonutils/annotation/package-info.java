/**
 * Marker annotations documenting nullability and numeric ranges on this library's public API. They
 * serve as human- and tool-readable contracts; validating methods in {@link org.commonutils.lang}
 * and {@link org.commonutils.util} enforce the contracts at runtime where stated (see each
 * annotation).
 *
 * <p><strong>Library consumers</strong>: these annotations do not implement validation. Applying
 * them to your own fields, parameters, methods, or types does not cause commonutils to enforce
 * anything at runtime; add your own checks or rely on static analysis or other tooling as needed.
 * Documented {@link java.lang.NullPointerException} and {@link java.lang.IllegalArgumentException}
 * behavior applies only where this library's public methods explicitly validate arguments (see each
 * method in {@link org.commonutils.lang} and {@link org.commonutils.util}).
 *
 * <h2>Annotations</h2>
 *
 * <ul>
 *   <li>{@link org.commonutils.annotation.Nullable} / {@link org.commonutils.annotation.NonNull}
 *       &mdash; reference may or must not be {@code null}
 *   <li>{@link org.commonutils.annotation.NonNegative} / {@link
 *       org.commonutils.annotation.Positive} &mdash; numeric range expectations
 * </ul>
 */
package org.commonutils.annotation;
