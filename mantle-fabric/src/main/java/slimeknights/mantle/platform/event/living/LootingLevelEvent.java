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

public class LootingLevelEvent extends LivingEvent {
  private final DamageSource damageSource;
  private int lootingLevel;

  public LootingLevelEvent(LivingEntity entity, DamageSource damageSource, int lootingLevel) {
    super(entity);
    this.damageSource = damageSource;
    this.lootingLevel = lootingLevel;
  }

  public DamageSource getDamageSource() {
    return damageSource;
  }

  public int getLootingLevel() {
    return lootingLevel;
  }

  public void setLootingLevel(int lootingLevel) {
    this.lootingLevel = lootingLevel;
  }
}
