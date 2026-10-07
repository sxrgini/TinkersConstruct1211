package slimeknights.mantle.platform.event.server;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import org.jetbrains.annotations.Nullable;
import slimeknights.mantle.platform.event.Event;
import slimeknights.mantle.platform.event.EventBus;

/** Fired when data pack data should be synced to a player, either on join or after a reload */
public class OnDatapackSyncEvent extends Event {
  private final PlayerList playerList;
  @Nullable
  private final ServerPlayer player;

  public OnDatapackSyncEvent(PlayerList playerList, @Nullable ServerPlayer player) {
    this.playerList = playerList;
    this.player = player;
  }

  public PlayerList getPlayerList() {
    return playerList;
  }

  /** Gets the player to sync to, if null sync to everyone in the list */
  @Nullable
  public ServerPlayer getPlayer() {
    return player;
  }

  /** Registers the Fabric callback that fires this event for each synced player */
  public static void init() {
    ServerLifecycleEvents.SYNC_DATA_PACK_CONTENTS.register((player, joined) ->
      EventBus.BUS.post(new OnDatapackSyncEvent(player.server.getPlayerList(), player)));
  }
}
