package slimeknights.mantle.platform.event.player;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import slimeknights.mantle.platform.event.Event.HasResult;

/** Fired when a player attacks, allowing modification of critical hits. Result ALLOW forces a critical, DENY prevents it. */
@HasResult
public class CriticalHitEvent extends PlayerEvent {
  private final Entity target;
  private final float oldDamageModifier;
  private float damageModifier;
  private final boolean vanillaCritical;

  public CriticalHitEvent(Player player, Entity target, float damageModifier, boolean vanillaCritical) {
    super(player);
    this.target = target;
    this.damageModifier = damageModifier;
    this.oldDamageModifier = damageModifier;
    this.vanillaCritical = vanillaCritical;
  }

  public Entity getTarget() {
    return target;
  }

  public float getOldDamageModifier() {
    return oldDamageModifier;
  }

  public float getDamageModifier() {
    return damageModifier;
  }

  public void setDamageModifier(float modifier) {
    this.damageModifier = modifier;
  }

  public boolean isVanillaCritical() {
    return vanillaCritical;
  }
}
