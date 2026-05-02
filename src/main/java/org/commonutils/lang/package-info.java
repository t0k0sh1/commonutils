/**
 * Language-level helpers: strings, character sequences, numeric strings, and wrappers aligned with
 * {@link java.util.Objects}. Prefer this package for text and scalar parsing; use {@link
 * org.commonutils.util} for collections and maps.
 *
 * <h2>Main entry points</h2>
 *
 * <ul>
 *   <li>{@link org.commonutils.lang.StringSupport} &mdash; empty/blank, trim/strip, padding, length
 *       counting, safe equality
 *   <li>{@link org.commonutils.lang.LengthCounter} / {@link org.commonutils.lang.Lengths} &mdash;
 *       how to count {@link java.lang.CharSequence} length (code units, code points, East Asian
 *       columns)
 *   <li>{@link org.commonutils.lang.NumberSupport} &mdash; digit checks, parsing with defaults,
 *       {@code clamp}
 *   <li>{@link org.commonutils.lang.ObjectSupport} &mdash; forwarding to {@code Objects} plus extra
 *       helpers
 * </ul>
 */
package org.commonutils.lang;
