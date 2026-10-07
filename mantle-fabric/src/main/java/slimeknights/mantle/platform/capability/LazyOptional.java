package slimeknights.mantle.platform.capability;

import slimeknights.mantle.platform.util.NonNullConsumer;
import slimeknights.mantle.platform.util.NonNullFunction;
import slimeknights.mantle.platform.util.NonNullPredicate;
import slimeknights.mantle.platform.util.NonNullSupplier;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Supplier;

/** Lazily resolved optional value that can be invalidated, replacing Forge's {@code LazyOptional}. */
@SuppressWarnings("unused")
public final class LazyOptional<T> {
  private static final LazyOptional<Object> EMPTY = new LazyOptional<>(null);

  @Nullable
  private final NonNullSupplier<T> supplier;
  @Nullable
  private T resolved;
  private boolean isValid = true;
  @Nullable
  private List<NonNullConsumer<LazyOptional<T>>> listeners;

  private LazyOptional(@Nullable NonNullSupplier<T> supplier) {
    this.supplier = supplier;
  }

  /** Creates an optional that resolves its value on first use */
  public static <T> LazyOptional<T> of(NonNullSupplier<T> supplier) {
    return new LazyOptional<>(supplier);
  }

  @SuppressWarnings("unchecked")
  public static <T> LazyOptional<T> empty() {
    return (LazyOptional<T>) EMPTY;
  }

  @Nullable
  private T getValue() {
    if (supplier == null || !isValid) {
      return null;
    }
    if (resolved == null) {
      resolved = supplier.get();
    }
    return resolved;
  }

  public boolean isPresent() {
    return supplier != null && isValid;
  }

  public void ifPresent(NonNullConsumer<? super T> consumer) {
    T value = getValue();
    if (value != null) {
      consumer.accept(value);
    }
  }

  public <U> LazyOptional<U> lazyMap(NonNullFunction<? super T,? extends U> mapper) {
    if (!isPresent()) {
      return empty();
    }
    return of(() -> mapper.apply(getValue()));
  }

  public <U> Optional<U> map(NonNullFunction<? super T,? extends U> mapper) {
    T value = getValue();
    return value == null ? Optional.empty() : Optional.ofNullable(mapper.apply(value));
  }

  public Optional<T> filter(NonNullPredicate<? super T> predicate) {
    T value = getValue();
    return value != null && predicate.test(value) ? Optional.of(value) : Optional.empty();
  }

  public Optional<T> resolve() {
    return Optional.ofNullable(getValue());
  }

  public T orElse(T other) {
    T value = getValue();
    return value != null ? value : other;
  }

  public T orElseGet(NonNullSupplier<? extends T> other) {
    T value = getValue();
    return value != null ? value : other.get();
  }

  public <X extends Throwable> T orElseThrow(Supplier<? extends X> exception) throws X {
    T value = getValue();
    if (value == null) {
      throw exception.get();
    }
    return value;
  }

  public T orElseThrow() {
    T value = getValue();
    if (value == null) {
      throw new NoSuchElementException("No value present");
    }
    return value;
  }

  /** Casts this optional, with no checking, to another type. Used to return a more specific capability from a generic method */
  @SuppressWarnings("unchecked")
  public <X> LazyOptional<X> cast() {
    return (LazyOptional<X>) this;
  }

  /** Adds a listener called when this optional is invalidated */
  public void addListener(NonNullConsumer<LazyOptional<T>> listener) {
    if (isValid && supplier != null) {
      if (listeners == null) {
        listeners = new ArrayList<>();
      }
      listeners.add(listener);
    } else {
      listener.accept(this);
    }
  }

  /** Removes a listener added with {@link #addListener(NonNullConsumer)} */
  public void removeListener(NonNullConsumer<LazyOptional<T>> listener) {
    if (listeners != null) {
      listeners.remove(listener);
    }
  }

  public void invalidate() {
    if (isValid && supplier != null) {
      isValid = false;
      if (listeners != null) {
        List<NonNullConsumer<LazyOptional<T>>> copy = listeners;
        listeners = null;
        copy.forEach(l -> l.accept(this));
      }
    }
  }
}
