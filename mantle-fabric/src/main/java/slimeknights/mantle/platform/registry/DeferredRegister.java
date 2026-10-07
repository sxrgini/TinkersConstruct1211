package slimeknights.mantle.platform.registry;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.registries.BuiltInRegistries;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Fabric replacement for NeoForge's {@code DeferredRegister}. Entries are queued and written to the registry when {@link #register()} is called, which should happen during mod initialization.
 * @param <T>  Registry type
 */
public class DeferredRegister<T> {
  protected final ResourceKey<? extends Registry<T>> registryKey;
  protected final String namespace;
  private final Map<DeferredHolder<T,? extends T>,Supplier<? extends T>> entries = new LinkedHashMap<>();
  private boolean registered = false;
  private final Map<ResourceLocation,ResourceLocation> aliases = new LinkedHashMap<>();

  public DeferredRegister(ResourceKey<? extends Registry<T>> registryKey, String namespace) {
    this.registryKey = registryKey;
    this.namespace = namespace;
  }

  /** Creates a new deferred register */
  public static <T> DeferredRegister<T> create(ResourceKey<? extends Registry<T>> registryKey, String namespace) {
    return new DeferredRegister<>(registryKey, namespace);
  }

  /** Gets the registry key for this register */
  public ResourceKey<? extends Registry<T>> getRegistryKey() {
    return registryKey;
  }

  /** Gets the namespace for this register */
  public String getNamespace() {
    return namespace;
  }

  /** Gets all queued entries */
  public Collection<DeferredHolder<T,? extends T>> getEntries() {
    return Collections.unmodifiableCollection(entries.keySet());
  }

  /** Creates the holder object for the given key, allows subclasses to use more specific holder types */
  protected <I extends T> DeferredHolder<T,I> createHolder(ResourceKey<? extends Registry<T>> registryKey, ResourceLocation key) {
    return DeferredHolder.create(ResourceKey.create(registryKey, key));
  }

  /** Queues a new entry using the given supplier */
  public <I extends T> DeferredHolder<T,I> register(String name, Supplier<? extends I> sup) {
    if (registered) {
      throw new IllegalStateException("Cannot register new entries to DeferredRegister after registration: " + namespace + ":" + name);
    }
    ResourceLocation id = ResourceLocation.fromNamespaceAndPath(namespace, name);
    DeferredHolder<T,I> holder = createHolder(registryKey, id);
    if (entries.putIfAbsent(holder, sup) != null) {
      throw new IllegalArgumentException("Duplicate registration " + id);
    }
    return holder;
  }

  /** Queues a new entry using the given function, which receives the entry ID */
  public <I extends T> DeferredHolder<T,I> register(String name, Function<ResourceLocation,? extends I> func) {
    return register(name, () -> func.apply(ResourceLocation.fromNamespaceAndPath(namespace, name)));
  }

  /** Adds an alias so the old name resolves to the new one, applied when registering */
  public void addAlias(ResourceLocation from, ResourceLocation to) {
    aliases.put(from, to);
  }

  /** Writes all queued entries to the registry. Call during mod initialization */
  @SuppressWarnings("unchecked")
  public void register() {
    if (registered) {
      throw new IllegalStateException("DeferredRegister already registered: " + namespace);
    }
    registered = true;
    Registry<T> registry = (Registry<T>) BuiltInRegistries.REGISTRY.get(registryKey.location());
    if (registry == null) {
      throw new IllegalStateException("Unknown registry " + registryKey.location());
    }
    entries.forEach((holder, supplier) -> Registry.register(registry, holder.getKey().location(), supplier.get()));
    // TODO: Fabric has no registry alias support in this version, aliases are only recorded
  }
}
