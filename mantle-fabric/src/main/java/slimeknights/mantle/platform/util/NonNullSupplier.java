package slimeknights.mantle.platform.util;

/** Supplier that never returns null, replacing Forge's {@code NonNullSupplier} */
@FunctionalInterface
public interface NonNullSupplier<T> {
  T get();
}
