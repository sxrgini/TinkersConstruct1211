package slimeknights.mantle.platform.event.client;

import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.Connection;
import org.jetbrains.annotations.Nullable;
import slimeknights.mantle.platform.event.Event;

/** Client connection events */
public abstract class ClientPlayerNetworkEvent extends Event {
  @Nullable
  private final MultiPlayerGameMode controller;
  @Nullable
  private final LocalPlayer player;
  @Nullable
  private final Connection connection;

  protected ClientPlayerNetworkEvent(@Nullable MultiPlayerGameMode controller, @Nullable LocalPlayer player, @Nullable Connection connection) {
    this.controller = controller;
    this.player = player;
    this.connection = connection;
  }

  @Nullable public MultiPlayerGameMode getMultiPlayerGameMode() { return controller; }
  @Nullable public LocalPlayer getPlayer() { return player; }
  @Nullable public Connection getConnection() { return connection; }

  /** Fired when the local player disconnects from a server */
  public static class LoggingOut extends ClientPlayerNetworkEvent {
    public LoggingOut(@Nullable MultiPlayerGameMode controller, @Nullable LocalPlayer player, @Nullable Connection connection) {
      super(controller, player, connection);
    }
  }
}
