package slimeknights.mantle.platform.util;

import java.util.function.Supplier;

/** Lazily computed value, replacing Forge's {@code Lazy} */
public interface Lazy<T> extends Supplier<T> {
  /** Creates a thread safe lazy value computed on first access */
  static <T> Lazy<T> of(Supplier<T> supplier) {
    return new Lazy<>() {
      private volatile boolean computed = false;
      private T value;

      @Override
      public T get() {
        if (!computed) {
          synchronized (this) {
            if (!computed) {
              value = supplier.get();
              computed = true;
            }
          }
        }
        return value;
      }
    };
  }
}
