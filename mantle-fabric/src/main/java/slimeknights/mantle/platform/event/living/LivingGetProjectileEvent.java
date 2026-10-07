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

public class LivingGetProjectileEvent extends LivingEvent {
  private final ItemStack projectileWeaponItemStack;
  private ItemStack projectileItemStack;

  public LivingGetProjectileEvent(LivingEntity entity, ItemStack projectileWeaponItemStack, ItemStack projectileItemStack) {
    super(entity);
    this.projectileWeaponItemStack = projectileWeaponItemStack;
    this.projectileItemStack = projectileItemStack;
  }

  public ItemStack getProjectileWeaponItemStack() {
    return projectileWeaponItemStack;
  }

  public ItemStack getProjectileItemStack() {
    return projectileItemStack;
  }

  public void setProjectileItemStack(ItemStack projectileItemStack) {
    this.projectileItemStack = projectileItemStack;
  }
}
