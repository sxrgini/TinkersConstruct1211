package slimeknights.mantle.platform.event.server;

import net.minecraft.server.MinecraftServer;
import slimeknights.mantle.platform.event.Event;

/** Fired when the server begins stopping */
public class ServerStoppingEvent extends Event {
  private final MinecraftServer server;

  public ServerStoppingEvent(MinecraftServer server) {
    this.server = server;
  }

  public MinecraftServer getServer() {
    return server;
  }
}
