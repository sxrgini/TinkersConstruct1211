package slimeknights.mantle.platform.util;

/** Function with a non null input and output, replacing Forge's {@code NonNullFunction} */
@FunctionalInterface
public interface NonNullFunction<T,R> {
  R apply(T t);
}
