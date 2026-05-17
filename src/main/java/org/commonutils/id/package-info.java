/**
 * Identifier generators with a shared {@link org.commonutils.id.IdGenerator IdGenerator} contract.
 *
 * <h2>Main entry points</h2>
 *
 * <ul>
 *   <li>{@link org.commonutils.id.IdGenerator} &mdash; common {@code generate()} procedure
 *   <li>{@link org.commonutils.id.UuidV4IdGenerator} &mdash; RFC 4122 version-4 UUIDs
 *   <li>{@link org.commonutils.id.UuidV7} &mdash; RFC 9562 version-7 UUIDs ({@link
 *       java.time.Instant}, {@link java.time.Clock}, batch and range helpers)
 *   <li>{@link org.commonutils.id.UuidV7IdGenerator} &mdash; {@link org.commonutils.id.IdGenerator}
 *       for RFC 9562 version-7 UUIDs (fixed {@link java.time.Instant} or per-call {@link
 *       java.time.Clock})
 *   <li>{@link org.commonutils.id.NanoIdGenerator} &mdash; short URL-safe random strings ({@link
 *       org.commonutils.id.NanoIdGenerator#DEFAULT_ALPHABET default alphabet} / configurable),
 *       unlike fixed-width binary UUIDs
 * </ul>
 *
 * <h2>Choosing UUID version</h2>
 *
 * <p><strong>Version 4</strong> (RFC 4122) embeds no timestamp: identifiers are random. Use {@link
 * org.commonutils.id.UuidV4IdGenerator} or {@link java.util.UUID#randomUUID()} when you need
 * opaque, unpredictable IDs without time semantics.
 *
 * <p><strong>Version 7</strong> (RFC 9562) carries Unix milliseconds in the most significant bits
 * for rough time order and sorting. Use {@link org.commonutils.id.UuidV7} or {@link
 * org.commonutils.id.UuidV7IdGenerator} when you need that encoding or test-friendly control via
 * {@link java.time.Clock}; version 4 deliberately does not accept {@code Clock} or date-time
 * parameters.
 */
package org.commonutils.id;
