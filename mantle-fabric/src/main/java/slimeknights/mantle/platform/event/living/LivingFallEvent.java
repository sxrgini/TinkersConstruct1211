package slimeknights.mantle.platform.event.living;

import java.util.Collection;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import slimeknights.mantle.platform.event.Event.Cancelable;

@Cancelable
public class LivingFallEvent extends LivingEvent {
  private float distance;
  private float damageMultiplier;

  public LivingFallEvent(LivingEntity entity, float distance, float damageMultiplier) {
    super(entity);
    this.distance = distance;
    this.damageMultiplier = damageMultiplier;
  }

  public float getDistance() {
    return distance;
  }

  public void setDistance(float distance) {
    this.distance = distance;
  }

  public float getDamageMultiplier() {
    return damageMultiplier;
  }

  public void setDamageMultiplier(float damageMultiplier) {
    this.damageMultiplier = damageMultiplier;
  }
}
