package slimeknights.mantle.platform.util;

/** Predicate on a non null value, replacing Forge's {@code NonNullPredicate} */
@FunctionalInterface
public interface NonNullPredicate<T> {
  boolean test(T t);
}
