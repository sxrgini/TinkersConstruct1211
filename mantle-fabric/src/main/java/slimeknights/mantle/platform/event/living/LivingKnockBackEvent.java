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
public class LivingKnockBackEvent extends LivingEvent {
  private float strength;
  private double ratioX;
  private double ratioZ;

  public LivingKnockBackEvent(LivingEntity entity, float strength, double ratioX, double ratioZ) {
    super(entity);
    this.strength = strength;
    this.ratioX = ratioX;
    this.ratioZ = ratioZ;
  }

  public float getStrength() {
    return strength;
  }

  public void setStrength(float strength) {
    this.strength = strength;
  }

  public double getRatioX() {
    return ratioX;
  }

  public void setRatioX(double ratioX) {
    this.ratioX = ratioX;
  }

  public double getRatioZ() {
    return ratioZ;
  }

  public void setRatioZ(double ratioZ) {
    this.ratioZ = ratioZ;
  }
}
