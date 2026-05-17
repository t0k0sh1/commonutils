/**
 * Small JDK 21+ utility library. The public API is <em>curated</em>: it favors high-frequency,
 * broadly useful helpers over exhaustive coverage of every category, and it is not limited to any
 * single domain—the set of packages and types may grow over time. Implementations aim to be safe
 * and fast and depend on no third-party libraries; this module requires only {@code java.base}.
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
 *   <dt>{@code org.commonutils.id}
 *   <dd>{@link org.commonutils.id.IdGenerator}, {@link org.commonutils.id.UuidV4IdGenerator},
 *       {@link org.commonutils.id.UuidV7}, {@link org.commonutils.id.UuidV7IdGenerator} (RFC 4122
 *       v4 and RFC 9562 v7 UUIDs), and {@link org.commonutils.id.NanoIdGenerator} (URL-safe string
 *       IDs).
 * </dl>
 *
 * <p>Non-exported packages (for example implementation details of validation) are not part of the
 * supported surface when using the module path.
 */
module org.commonutils {
  requires java.base;

  exports org.commonutils.annotation;
  exports org.commonutils.id;
  exports org.commonutils.lang;
  exports org.commonutils.util;
}
