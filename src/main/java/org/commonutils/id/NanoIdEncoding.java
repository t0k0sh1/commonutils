package org.commonutils.id;

import java.util.HashSet;
import java.util.Objects;
import java.util.random.RandomGenerator;
import org.commonutils.annotation.NonNull;
import org.commonutils.internal.Contracts;

/**
 * Shared Nano ID-style alphabet validation, bitmask sizing, bias-free index sampling, and string
 * generation for implementations in this package.
 */
final class NanoIdEncoding {

  private NanoIdEncoding() {}

  /**
   * Validates {@code alphabet} and returns its BMP code units as a char array (order preserved).
   *
   * @throws IllegalArgumentException if {@code alphabet} is empty, has duplicates, or contains
   *     non-BMP or surrogate code points
   */
  static char @NonNull [] alphabetCharsFrom(final String alphabet) {
    Objects.requireNonNull(alphabet, "alphabet");
    if (alphabet.isEmpty()) {
      throw new IllegalArgumentException("alphabet must not be empty");
    }
    final int[] cps = alphabet.codePoints().toArray();
    final HashSet<Integer> seen = new HashSet<>();
    final char[] chars = new char[cps.length];
    for (int i = 0; i < cps.length; i++) {
      final int cp = cps[i];
      if (cp > Character.MAX_VALUE) {
        throw new IllegalArgumentException("alphabet must use BMP code points only");
      }
      if (cp >= Character.MIN_SURROGATE && cp <= Character.MAX_SURROGATE) {
        throw new IllegalArgumentException("alphabet must not contain surrogate code points");
      }
      if (!seen.add(cp)) {
        throw new IllegalArgumentException("alphabet code points must be unique");
      }
      chars[i] = (char) cp;
    }
    return chars;
  }

  /** Same rule as Nano ID: smallest {@code (2^k - 1)} with {@code mask >= alphabetLength - 1}. */
  static int maskForAlphabetLength(final int alphabetLength) {
    Contracts.requirePositive("alphabetLength", alphabetLength);
    final int clz = Integer.numberOfLeadingZeros((alphabetLength - 1) | 1);
    final int shift = 31 - clz;
    if (shift < 0 || shift >= 31) {
      throw new IllegalArgumentException("alphabetLength (" + alphabetLength + ") is too large");
    }
    return (2 << shift) - 1;
  }

  /**
   * Builds a Nano ID-style string of length {@code size} using bias-free masked rejection sampling.
   */
  static @NonNull String generateNanoId(
      final RandomGenerator rng,
      final char @NonNull [] alphabetChars,
      final int mask,
      final int size) {
    Objects.requireNonNull(rng, "rng");
    Objects.requireNonNull(alphabetChars, "alphabetChars");
    Contracts.requirePositive("size", size);
    final char[] out = new char[size];
    final byte[] scratch = new byte[4];
    final int len = alphabetChars.length;
    for (int i = 0; i < size; i++) {
      out[i] = alphabetChars[nextIndex(rng, scratch, mask, len)];
    }
    return new String(out);
  }

  private static int nextIndex(
      final RandomGenerator rng, final byte[] scratch, final int mask, final int alphabetLength) {
    while (true) {
      rng.nextBytes(scratch);
      final int r =
          (scratch[0] & 0xFF)
              | ((scratch[1] & 0xFF) << 8)
              | ((scratch[2] & 0xFF) << 16)
              | ((scratch[3] & 0xFF) << 24);
      final int idx = r & mask;
      if (idx < alphabetLength) {
        return idx;
      }
    }
  }
}
