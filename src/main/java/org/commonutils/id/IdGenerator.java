package org.commonutils.id;

import org.commonutils.annotation.NonNull;

/**
 * Generates identifiers of type {@code T}. Implementations are expected to return a non-null value
 * on every invocation unless documented otherwise; this library validates non-null contracts on
 * public APIs where stated.
 *
 * @param <T> identifier type produced by {@link #generate()}
 * @since 0.2.0
 */
@FunctionalInterface
public interface IdGenerator<T> {

  /**
   * Creates the next identifier. Implementations in this module validate their contracts and return
   * a non-null {@link java.util.UUID UUID} for {@link UuidV4IdGenerator}.
   *
   * @return a new identifier, never {@code null}
   * @since 0.2.0
   */
  @NonNull
  T generate();
}
