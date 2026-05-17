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
 *   <li>{@link org.commonutils.id.Ulid} &mdash; ULID strings (48-bit Unix milliseconds + 80-bit
 *       randomness, Crockford Base32; {@link org.commonutils.id.Ulid#fromInstant(java.time.Instant)
 *       fromInstant} / {@link org.commonutils.id.Ulid#fromClock(java.time.Clock) fromClock} and
 *       {@link org.commonutils.id.Ulid#encode(long, byte[])} for tests)
 *   <li>{@link org.commonutils.id.UlidIdGenerator} &mdash; {@link org.commonutils.id.IdGenerator}
 *       for ULIDs (fixed {@link java.time.Instant} or per-call {@link java.time.Clock}); {@link
 *       org.commonutils.id.UlidIdGenerator#nonCryptographic() nonCryptographic} uses {@link
 *       java.util.SplittableRandom}-backed entropy (see class Javadoc)
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
 * <h2>ULID vs UUID v7 vs Nano ID</h2>
 *
 * <p><strong>ULID</strong> ({@link org.commonutils.id.Ulid} / {@link
 * org.commonutils.id.UlidIdGenerator}) encodes the same 48-bit Unix millisecond idea as {@link
 * org.commonutils.id.UuidV7} into a fixed-length, lexicographically sortable Crockford Base32
 * <em>string</em>, which is convenient for human-readable keys and many text-first storage formats.
 *
 * <p><strong>UUID version 7</strong> is a standardized binary UUID ({@link java.util.UUID}) in RFC
 * 9562; use {@link org.commonutils.id.UuidV7} / {@link org.commonutils.id.UuidV7IdGenerator} when
 * you need UUID types and tooling.
 *
 * <p><strong>Nano ID</strong> ({@link org.commonutils.id.NanoIdGenerator}) produces shorter,
 * URL-safe strings with a configurable alphabet and length; unlike ULID it does not embed a
 * timestamp in a standardized layout.
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
