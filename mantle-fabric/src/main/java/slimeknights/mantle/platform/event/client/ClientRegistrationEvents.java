package slimeknights.mantle.platform.event.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import slimeknights.mantle.platform.event.EventBus;

/** Fires all client registration events on the mod bus, call from the mod initializer on the client after listeners are registered */
@Environment(EnvType.CLIENT)
public final class ClientRegistrationEvents {
  private ClientRegistrationEvents() {}

  /** Fires the events, the namespace is used for geometry loader names */
  public static void fireAll(String namespace) {
    EventBus bus = EventBus.MOD_BUS;
    bus.post(new ModelEvent.RegisterGeometryLoaders(namespace));
    bus.post(new ModelEvent.RegisterAdditional());
    bus.post(new RegisterColorHandlersEvent.Block());
    bus.post(new RegisterColorHandlersEvent.Item());
    bus.post(new EntityRenderersEvent.RegisterRenderers());
    bus.post(new EntityRenderersEvent.RegisterLayerDefinitions());
    bus.post(new EntityRenderersEvent.CreateSkullModels());
    bus.post(new RegisterParticleProvidersEvent());
    bus.post(new RegisterKeyMappingsEvent());
    bus.post(new RegisterClientReloadListenersEvent());
  }
}
