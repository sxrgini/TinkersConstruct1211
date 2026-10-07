package slimeknights.mantle.platform.network;

import net.minecraft.world.entity.player.Player;

/** Context passed to packet handlers, mirrors the parts of NeoForge's {@code IPayloadContext} Mantle uses. */
public interface IPayloadContext {
  /** Gets the player associated with the packet: the sender on the server, the local player on the client */
  Player player();

  /** Runs the task on the main thread. Fabric already dispatches handlers to the main thread, so this runs immediately. */
  default void enqueueWork(Runnable task) {
    task.run();
  }

  /** Forge style accessor for the sending player, null if this is not the server */
  @javax.annotation.Nullable
  default net.minecraft.server.level.ServerPlayer getSender() {
    return player() instanceof net.minecraft.server.level.ServerPlayer server ? server : null;
  }
}
