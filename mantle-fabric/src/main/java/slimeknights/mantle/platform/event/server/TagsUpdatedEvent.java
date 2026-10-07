package slimeknights.mantle.platform.event.server;

import net.minecraft.core.RegistryAccess;
import slimeknights.mantle.platform.event.Event;

/** Fired after tags are loaded or synced to the client */
public class TagsUpdatedEvent extends Event {
  private final RegistryAccess access;
  private final boolean client;

  public TagsUpdatedEvent(RegistryAccess access, boolean client) {
    this.access = access;
    this.client = client;
  }

  public RegistryAccess getRegistryAccess() {
    return access;
  }

  /** If true, tags were synced to a client, otherwise tags were loaded on the server */
  public boolean isFromClientPacket() {
    return client;
  }
}
