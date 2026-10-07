package slimeknights.mantle.platform.event.client;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import slimeknights.mantle.Mantle;
import slimeknights.mantle.platform.client.ClientReloadListeners;
import slimeknights.mantle.platform.event.Event;

import java.util.Locale;

/** Event to register client resource reload listeners, fired by the client initializer */
public class RegisterClientReloadListenersEvent extends Event {
  private int index = 0;

  /** Registers the given listener, ids are generated in registration order so ordering is preserved */
  public void registerReloadListener(PreparableReloadListener listener) {
    ClientReloadListeners.register(ResourceLocation.fromNamespaceAndPath(Mantle.modId, "client_wrapped/" + (index++) + "_" + listener.getClass().getSimpleName().toLowerCase(Locale.ROOT)), listener);
  }
}
