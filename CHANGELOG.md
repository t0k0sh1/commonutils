# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [0.2.0] - YYYY-MM-DD

### Added

- **`MapSupport.size`**: returns `Map.size()` when the map is non-null, or `0` when it is `null`; throws `IllegalArgumentException` if `size()` is negative (same contract as `CollectionSupport.size`).
- **Tests**: `MapSupportTest` coverage for `MapSupport.size`.
- **`LengthCounter`**: functional strategy for measuring `CharSequence` length; `null` sequences count as 0.
- **`Lengths`**: built-in enum with `UNITS` (UTF-16 code units, same as `CharSequence.length()`), `POINTS` (Unicode code points; not grapheme clusters), and `EAW` (East Asian display columns per UAX #11: F/W/A → 2 columns, H/Na/N → 1; unlisted code points → 1).
- **`StringSupport.length`**: one-arg form uses `UNITS`; two-arg form takes a `LengthCounter` (non-null); `how` null throws `NullPointerException`.
- **`EastAsianWidthColumns`** (non-exported `org.commonutils.internal`): data derived from Unicode 15.1.0 `EastAsianWidth.txt`.
- **Tests**: `StringSupportTest` coverage for length APIs; `StringSupportLengthEdgeCasesTest` for ill-formed UTF-16, combining marks, regional indicators, VS16, and EAW fallbacks.
- **`org.commonutils.id`** (JPMS export and package docs): identifier generators with a shared contract.
- **`IdGenerator`**: `@FunctionalInterface` with `generate()` as the common procedure for all generators in this package.
- **`UuidV4IdGenerator`**: RFC 4122 version-4 UUIDs; defaults to `UUID.randomUUID()`, or accepts a `RandomGenerator` for tests and custom entropy (validated; `NullPointerException` when null).
- **`UuidV7`**: RFC 9562 version-7 UUIDs from 48-bit Unix epoch milliseconds (`Instant.toEpochMilli()` semantics) plus random payload; `Clock` overloads; batch APIs (same instant, repeated clock sampling, non-decreasing timestamps within a closed millisecond range); validates non-null references and representable timestamps (`NullPointerException`, `IllegalArgumentException`).
- **`UuidV7IdGenerator`**: `IdGenerator` for UUID v7 using either a fixed `Instant` or `Clock` per `generate()` call; optional `RandomGenerator` (validated; defaults to cryptographically strong entropy).
- **Tests**: `UuidV4IdGeneratorTest` for version/variant, canonical lowercase string form, deterministic output with a seeded `RandomGenerator`, and null rejection.
- **Tests**: `UuidV7Test` (RFC 9562 appendix test vector, bounds, batch ordering, clock usage); `UuidV7IdGeneratorTest`.
- **`NanoIdGenerator`**: Nano ID-style URL-safe strings with bias-free index selection; default alphabet and length match the reference implementation; optional custom alphabet and size; cryptographically strong `RandomGenerator` by default or an injected `RandomGenerator` for tests; **`nonCryptographic()`** / **`nonCryptographic(int)`** / **`nonCryptographic(String, int)`** use **`SplittableRandom`** for throughput (documented as unsuitable for security-sensitive identifiers).
- **`MutableNanoIdGenerator`**: same encoding rules as **`NanoIdGenerator`** with runtime **`configure`**, **`setAlphabet`**, and **`setSize`** (compare-and-set loops for concurrent updates; snapshot-per-**`generate`**); **`nonCryptographic…`** factories mirror **`NanoIdGenerator`**.
- **`Ulid`**: [ULID](https://github.com/ulid/spec) strings (48-bit Unix `Instant#toEpochMilli()` timestamp + 80 bits randomness, Crockford Base32); **`fromInstant`** / **`fromClock`** (default `SecureRandom` or injected `RandomGenerator`); **`encode(long, byte[])`** for deterministic tests; validates timestamp range like `UuidV7`.
- **`UlidIdGenerator`**: **`IdGenerator<String>`** for ULIDs with fixed **`Instant`** or `Clock`, mirroring **`UuidV7IdGenerator`**; **`nonCryptographic()`** / **`nonCryptographic(Clock)`** use `SplittableRandom` (documented as not for security-sensitive identifiers).
- **Tests**: **`NanoIdGeneratorTest`** for shape, deterministic output with a seeded **`RandomGenerator`**, successive values, alphabet contracts, null rejection, and **`nonCryptographic…`**; **`MutableNanoIdGeneratorTest`** for mutation contracts and defaults.
- **Tests**: **`UlidTest`** (encoding vectors, bounds, shape, clock stepping); **`UlidIdGeneratorTest`**.
- **`Cuid2`**: helpers for <a href="https://github.com/paralleldrive/cuid2">Cuid2</a>-compatible strings (`SHA3-512`, base36, default entropy-only fingerprint matching the reference when no global keys exist); **`isValid`**, **`createDefaultFingerprint`**, **`generate`** (for tests and advanced call sites).
- **`Cuid2IdGenerator`**: **`IdGenerator<String>`** for Cuid2 (length **2–32**, default **24**); **`Clock`** and **`RandomGenerator`** injection; custom fingerprint and fixed initial counter overloads for deployment- or test-specific control; **`nonCryptographic()`** / **`nonCryptographic(int)`** use **`SplittableRandom`** (documented as not for security-sensitive identifiers).
- **Tests**: **`Cuid2IdGeneratorTest`** (golden sequence, shape, bounds, distinct batch, null/empty rejection).

### Changed

- **`org.commonutils.lang` package docs**: mention `LengthCounter` / `Lengths` and `StringSupport` length counting.
- **`org.commonutils.id`**: package documentation adds “Choosing UUID version” (v4 random vs v7 timestamp / `Clock`); **`UuidV4IdGenerator`** class Javadoc cross-links **`UuidV7`** / **`UuidV7IdGenerator`** for time-based use cases (GitHub issue #8).
- **`org.commonutils.id`** / **`IdGenerator`**: package docs add **`MutableNanoIdGenerator`**, **`Ulid`** / **`UlidIdGenerator`**, **`Cuid2`** / **`Cuid2IdGenerator`**, a **`Nano ID generators`** section, ULID vs UUID v7 vs Nano ID vs Cuid2 guidance, and encoding-scope guidance; **`IdGenerator`** documents **`MutableNanoIdGenerator`**, **`UlidIdGenerator`**, and **`Cuid2IdGenerator`** for string ids (GitHub issue #10).

### Notes

- **Nano ID ecosystem encodings / plugin compatibility**: not shipped as APIs in this library until there is an agreed specification and compatibility policy (GitHub issue #10).

## [0.1.0] - 2026-05-02

### Added

- **`org.commonutils` JPMS module** with a runtime dependency on `java.base` only.
- **`org.commonutils.lang`**: `StringSupport`, `NumberSupport`, and `ObjectSupport` (string, number, and `Objects`-aligned helpers).
- **`org.commonutils.util`**: `CollectionSupport` and `MapSupport` (collection and map helpers).
- **`org.commonutils.annotation`**: contract markers `Nullable`, `NonNull`, `NonNegative`, and `Positive` (public APIs that use them perform runtime validation where applicable).

### Notes

- Requires **JDK 21+**.
