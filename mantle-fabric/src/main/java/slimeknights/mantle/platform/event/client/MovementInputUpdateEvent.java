package slimeknights.mantle.platform.event.client;

import net.minecraft.client.player.Input;
import net.minecraft.world.entity.player.Player;
import slimeknights.mantle.platform.event.Event;

/** Fired after the movement input is updated for the local player, fired by {@code KeyboardInputMixin} */
public class MovementInputUpdateEvent extends Event {
  private final Player player;
  private final Input input;

  public MovementInputUpdateEvent(Player player, Input input) {
    this.player = player;
    this.input = input;
  }

  public Player getEntity() { return player; }
  public Input getInput() { return input; }
}
