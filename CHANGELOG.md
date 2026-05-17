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
- **Tests**: `UuidV4IdGeneratorTest` for version/variant, canonical lowercase string form, deterministic output with a seeded `RandomGenerator`, and null rejection.

### Changed

- **`org.commonutils.lang` package docs**: mention `LengthCounter` / `Lengths` and `StringSupport` length counting.

## [0.1.0] - 2026-05-02

### Added

- **`org.commonutils` JPMS module** with a runtime dependency on `java.base` only.
- **`org.commonutils.lang`**: `StringSupport`, `NumberSupport`, and `ObjectSupport` (string, number, and `Objects`-aligned helpers).
- **`org.commonutils.util`**: `CollectionSupport` and `MapSupport` (collection and map helpers).
- **`org.commonutils.annotation`**: contract markers `Nullable`, `NonNull`, `NonNegative`, and `Positive` (public APIs that use them perform runtime validation where applicable).

### Notes

- Requires **JDK 21+**.
