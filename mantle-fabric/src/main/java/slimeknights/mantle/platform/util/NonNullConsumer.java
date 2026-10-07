package slimeknights.mantle.platform.util;

/** Consumer of a non null value, replacing Forge's {@code NonNullConsumer} */
@FunctionalInterface
public interface NonNullConsumer<T> {
  void accept(T t);
}
