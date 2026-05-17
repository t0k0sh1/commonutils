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
 *   <li>{@link org.commonutils.id.MutableNanoIdGenerator} &mdash; Nano ID-style strings whose
 *       alphabet and length can change after construction ({@link
 *       org.commonutils.id.MutableNanoIdGenerator#configure configure} / {@link
 *       org.commonutils.id.MutableNanoIdGenerator#setAlphabet setAlphabet} / {@link
 *       org.commonutils.id.MutableNanoIdGenerator#setSize setSize}); prefer {@link
 *       org.commonutils.id.NanoIdGenerator} when configuration is fixed
 * </ul>
 *
 * <h2>Nano ID generators</h2>
 *
 * <p>Defaults mirror the reference <a href="https://github.com/ai/nanoid">Nano ID</a> alphabet and
 * length. Static {@code nonCryptographic…} factories on {@link org.commonutils.id.NanoIdGenerator}
 * and {@link org.commonutils.id.MutableNanoIdGenerator} opt into a fast {@link
 * java.util.SplittableRandom}-backed {@link java.util.random.RandomGenerator}; see each class's API
 * note in its class documentation for security guidance.
 *
 * <p><strong>Ecosystem-specific encodings</strong> (alternate alphabets imposed by plugins or
 * external tools without a stable specification here) are intentionally not implemented in this
 * library until there is an agreed format and compatibility policy.
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
