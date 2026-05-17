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
 * </ul>
 */
package org.commonutils.id;
