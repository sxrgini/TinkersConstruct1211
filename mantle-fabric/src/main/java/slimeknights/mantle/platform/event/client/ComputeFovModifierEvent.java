package slimeknights.mantle.platform.event.client;

import net.minecraft.world.entity.player.Player;
import slimeknights.mantle.platform.event.Event;

/** Fired when the field of view modifier of a player is computed, fired by {@code AbstractClientPlayerMixin} */
public class ComputeFovModifierEvent extends Event {
  private final Player player;
  private final float fovModifier;
  private float newFovModifier;

  public ComputeFovModifierEvent(Player player, float fovModifier) {
    this.player = player;
    this.fovModifier = fovModifier;
    this.newFovModifier = fovModifier;
  }

  public Player getPlayer() { return player; }
  public float getFovModifier() { return fovModifier; }
  public float getNewFovModifier() { return newFovModifier; }
  public void setNewFovModifier(float newFovModifier) { this.newFovModifier = newFovModifier; }
}
