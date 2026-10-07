package slimeknights.mantle.platform.event.client;

import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.minecraft.resources.ResourceLocation;
import slimeknights.mantle.platform.client.model.GeometryLoaders;
import slimeknights.mantle.platform.client.model.IGeometryLoader;
import slimeknights.mantle.platform.event.Event;
import slimeknights.mantle.platform.event.lifecycle.IModBusEvent;

/** Model loading events, backed by Fabric's model loading plugin */
public abstract class ModelEvent extends Event implements IModBusEvent {
  /** Registers geometry loaders for the {@code loader} key in model JSON */
  public static class RegisterGeometryLoaders extends ModelEvent {
    private final String namespace;

    public RegisterGeometryLoaders(String namespace) {
      this.namespace = namespace;
    }

    public void register(String name, IGeometryLoader<?> loader) {
      GeometryLoaders.register(ResourceLocation.fromNamespaceAndPath(namespace, name), loader);
    }
  }

  /** Registers additional models to bake that are not referenced by a block or item */
  public static class RegisterAdditional extends ModelEvent {
    public void register(ResourceLocation model) {
      ModelLoadingPlugin.register(context -> context.addModels(model));
    }
  }
}
