package slimeknights.mantle.platform.event.player;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import slimeknights.mantle.platform.event.Event.Cancelable;

/** Fired when a player attacks an entity, before damage is calculated */
@Cancelable
public class AttackEntityEvent extends PlayerEvent {
  private final Entity target;

  public AttackEntityEvent(Player player, Entity target) {
    super(player);
    this.target = target;
  }

  public Entity getTarget() {
    return target;
  }
}
