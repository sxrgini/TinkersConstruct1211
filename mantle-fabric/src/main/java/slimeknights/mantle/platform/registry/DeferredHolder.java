package slimeknights.mantle.platform.registry;

import com.mojang.datafixers.util.Either;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderOwner;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * Fabric replacement for NeoForge's {@code DeferredHolder}. Knows its key up front and resolves to the real registry holder once registered.
 * @param <R>  Registry type
 * @param <T>  Value type
 */
public class DeferredHolder<R, T extends R> implements Holder<T>, Supplier<T> {
  private final ResourceKey<R> key;
  @Nullable
  private Holder<R> holder;

  protected DeferredHolder(ResourceKey<R> key) {
    this.key = Objects.requireNonNull(key);
  }

  /** Creates a holder for the given key */
  public static <R, T extends R> DeferredHolder<R,T> create(ResourceKey<R> key) {
    return new DeferredHolder<>(key);
  }

  /** Creates a holder for the given registry and ID */
  public static <R, T extends R> DeferredHolder<R,T> create(ResourceKey<? extends Registry<R>> registry, ResourceLocation id) {
    return new DeferredHolder<>(ResourceKey.create(registry, id));
  }

  /** Gets the key of this holder */
  public ResourceKey<R> getKey() {
    return key;
  }

  /** Gets the ID of this holder */
  public ResourceLocation getId() {
    return key.location();
  }

  /** Tries to resolve the real holder, returning null if not yet registered */
  @Nullable
  @SuppressWarnings("unchecked")
  private Holder<T> resolve() {
    if (holder == null) {
      Registry<R> registry = (Registry<R>) BuiltInRegistries.REGISTRY.get(key.registry());
      if (registry != null) {
        holder = registry.getHolder(key).orElse(null);
      }
    }
    return (Holder<T>) holder;
  }

  private Holder<T> requireHolder() {
    Holder<T> resolved = resolve();
    if (resolved == null) {
      throw new NullPointerException("Trying to access unbound value: " + key);
    }
    return resolved;
  }

  @Override
  public T get() {
    return value();
  }

  @Override
  public T value() {
    return requireHolder().value();
  }

  @Override
  public boolean isBound() {
    Holder<T> resolved = resolve();
    return resolved != null && resolved.isBound();
  }

  @Override
  public boolean is(ResourceLocation id) {
    return id.equals(key.location());
  }

  @Override
  public boolean is(ResourceKey<T> otherKey) {
    return key == otherKey || key.equals(otherKey);
  }

  @Override
  public boolean is(Predicate<ResourceKey<T>> predicate) {
    return predicate.test(getKeyAsT());
  }

  @SuppressWarnings("unchecked")
  private ResourceKey<T> getKeyAsT() {
    return (ResourceKey<T>) key;
  }

  @Override
  public boolean is(TagKey<T> tag) {
    Holder<T> resolved = resolve();
    return resolved != null && resolved.is(tag);
  }

  @SuppressWarnings("deprecation")
  @Override
  public boolean is(Holder<T> other) {
    return other.is(getKeyAsT());
  }

  @Override
  public Stream<TagKey<T>> tags() {
    Holder<T> resolved = resolve();
    return resolved == null ? Stream.empty() : resolved.tags();
  }

  @Override
  public Either<ResourceKey<T>,T> unwrap() {
    return Either.left(getKeyAsT());
  }

  @Override
  public Optional<ResourceKey<T>> unwrapKey() {
    return Optional.of(getKeyAsT());
  }

  @Override
  public Kind kind() {
    return Kind.REFERENCE;
  }

  @Override
  public boolean canSerializeIn(HolderOwner<T> owner) {
    Holder<T> resolved = resolve();
    return resolved != null && resolved.canSerializeIn(owner);
  }

  @Override
  public int hashCode() {
    return key.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    return obj == this || (obj instanceof DeferredHolder<?,?> other && key.equals(other.key));
  }

  @Override
  public String toString() {
    return "DeferredHolder{" + key + "}";
  }
}
