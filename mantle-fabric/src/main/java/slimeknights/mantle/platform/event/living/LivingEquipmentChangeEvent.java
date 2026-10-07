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

public class LivingEquipmentChangeEvent extends LivingEvent {
  private final EquipmentSlot slot;
  private final ItemStack from;
  private final ItemStack to;

  public LivingEquipmentChangeEvent(LivingEntity entity, EquipmentSlot slot, ItemStack from, ItemStack to) {
    super(entity);
    this.slot = slot;
    this.from = from;
    this.to = to;
  }

  public EquipmentSlot getSlot() {
    return slot;
  }

  public ItemStack getFrom() {
    return from;
  }

  public ItemStack getTo() {
    return to;
  }
}
