package slimeknights.mantle.platform.event.lifecycle;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import slimeknights.mantle.platform.event.Event;
import slimeknights.mantle.platform.event.EventBus;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Equivalent of Forge's register event, fired once per vanilla registry on the mod bus while registries are still open.
 * Call {@link #fireAll()} from the mod initializer once all listeners have been added to the bus.
 */
public class RegisterEvent extends Event implements IModBusEvent {
  private final ResourceKey<? extends Registry<?>> registryKey;

  public RegisterEvent(ResourceKey<? extends Registry<?>> registryKey) {
    this.registryKey = registryKey;
  }

  /** Gets the registry this event is for */
  public ResourceKey<? extends Registry<?>> getRegistryKey() {
    return registryKey;
  }

  /** Registers an object if the passed key matches this event */
  @SuppressWarnings("unchecked")
  public <T> void register(ResourceKey<? extends Registry<T>> key, ResourceLocation id, Supplier<T> supplier) {
    if (key.equals(registryKey)) {
      Registry<T> registry = (Registry<T>) BuiltInRegistries.REGISTRY.get(key.location());
      Registry.register(registry, id, supplier.get());
    }
  }

  /** Helper for bulk registering */
  public interface RegisterHelper<T> {
    void register(ResourceLocation id, T value);
  }

  /** Registers objects if the passed key matches this event */
  @SuppressWarnings("unchecked")
  public <T> void register(ResourceKey<? extends Registry<T>> key, Consumer<RegisterHelper<T>> consumer) {
    if (key.equals(registryKey)) {
      Registry<T> registry = (Registry<T>) BuiltInRegistries.REGISTRY.get(key.location());
      consumer.accept((id, value) -> Registry.register(registry, id, value));
    }
  }

  /** Fires the event for every registry on the mod bus */
  public static void fireAll() {
    for (ResourceKey<? extends Registry<?>> key : BuiltInRegistries.REGISTRY.registryKeySet()) {
      EventBus.MOD_BUS.post(new RegisterEvent(key));
    }
  }
}
