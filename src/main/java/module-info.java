/**
 * Small JDK 21+ utility library: null-tolerant string and collection helpers, map accessors,
 * numeric string parsing, and marker annotations for API contracts.
 *
 * <h2>Depend on this module</h2>
 *
 * {@code module-info.java} of the consumer should declare {@code requires org.commonutils;}. This
 * module depends only on {@code java.base}.
 *
 * <h2>Exported packages</h2>
 *
 * <dl>
 *   <dt>{@code org.commonutils.lang}
 *   <dd>String, number, and {@link java.util.Objects}-aligned helpers ({@link
 *       org.commonutils.lang.StringSupport}, {@link org.commonutils.lang.NumberSupport}, {@link
 *       org.commonutils.lang.ObjectSupport}).
 *   <dt>{@code org.commonutils.util}
 *   <dd>{@link java.util.Collection} and {@link java.util.Map} helpers ({@link
 *       org.commonutils.util.CollectionSupport}, {@link org.commonutils.util.MapSupport}).
 *   <dt>{@code org.commonutils.annotation}
 *   <dd>Contract markers: {@link org.commonutils.annotation.Nullable}, {@link
 *       org.commonutils.annotation.NonNull}, {@link org.commonutils.annotation.NonNegative}, {@link
 *       org.commonutils.annotation.Positive}.
 * </dl>
 *
 * <p>Non-exported packages (for example implementation details of validation) are not part of the
 * supported surface when using the module path.
 */
module org.commonutils {
  requires java.base;

  exports org.commonutils.annotation;
  exports org.commonutils.lang;
  exports org.commonutils.util;
}
