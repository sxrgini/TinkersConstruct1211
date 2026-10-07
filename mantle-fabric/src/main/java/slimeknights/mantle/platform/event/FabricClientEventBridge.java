package slimeknights.mantle.platform.event;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

/** Fires bus events for client side Fabric callbacks */
@Environment(EnvType.CLIENT)
public final class FabricClientEventBridge {
  private FabricClientEventBridge() {}

  /** Registers the client callbacks, call from the client initializer */
  public static void init() {
    EventBus bus = EventBus.BUS;
    ClientTickEvents.START_CLIENT_TICK.register(client -> {
      if (client.player != null && bus.hasListeners(TickEvent.PlayerTickEvent.class)) {
        bus.post(new TickEvent.PlayerTickEvent(TickEvent.Phase.START, LogicalSide.CLIENT, client.player));
      }
    });
    ClientTickEvents.END_CLIENT_TICK.register(client -> {
      if (client.player != null && bus.hasListeners(TickEvent.PlayerTickEvent.class)) {
        bus.post(new TickEvent.PlayerTickEvent(TickEvent.Phase.END, LogicalSide.CLIENT, client.player));
      }
    });
  }
}
