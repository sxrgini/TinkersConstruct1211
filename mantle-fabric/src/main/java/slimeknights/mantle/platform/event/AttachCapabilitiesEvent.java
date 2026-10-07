package slimeknights.mantle.platform.event;

import net.minecraft.resources.ResourceLocation;
import slimeknights.mantle.platform.capability.ICapabilityProvider;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Lets mods add capability providers to entities and item stacks, replacing Forge's {@code AttachCapabilitiesEvent}.
 * Fired lazily the first time capabilities of the object are queried through {@link slimeknights.mantle.platform.capability.Caps}.
 */
public class AttachCapabilitiesEvent<T> extends Event {
  private final T object;
  private final Map<ResourceLocation,ICapabilityProvider> caps = new LinkedHashMap<>();

  public AttachCapabilitiesEvent(T object) {
    this.object = object;
  }

  public T getObject() {
    return object;
  }

  public void addCapability(ResourceLocation key, ICapabilityProvider provider) {
    if (caps.containsKey(key)) {
      throw new IllegalStateException("Duplicate capability key " + key + " for " + object);
    }
    caps.put(key, provider);
  }

  /** Listener for when the capabilities are invalidated, invalidation is not currently fired on Fabric */
  public void addListener(Runnable listener) {}

  public Map<ResourceLocation,ICapabilityProvider> getCapabilities() {
    return Collections.unmodifiableMap(caps);
  }
}
