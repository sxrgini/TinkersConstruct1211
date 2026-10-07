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
public class ShieldBlockEvent extends LivingEvent {
  private final DamageSource damageSource;
  private float blockedDamage;
  private boolean shieldTakesDamage;

  public ShieldBlockEvent(LivingEntity entity, DamageSource damageSource, float blockedDamage, boolean shieldTakesDamage) {
    super(entity);
    this.damageSource = damageSource;
    this.blockedDamage = blockedDamage;
    this.shieldTakesDamage = shieldTakesDamage;
  }

  public DamageSource getDamageSource() {
    return damageSource;
  }

  public float getBlockedDamage() {
    return blockedDamage;
  }

  public void setBlockedDamage(float blockedDamage) {
    this.blockedDamage = blockedDamage;
  }

  public boolean getShieldTakesDamage() {
    return shieldTakesDamage;
  }

  public void setShieldTakesDamage(boolean shieldTakesDamage) {
    this.shieldTakesDamage = shieldTakesDamage;
  }
}
