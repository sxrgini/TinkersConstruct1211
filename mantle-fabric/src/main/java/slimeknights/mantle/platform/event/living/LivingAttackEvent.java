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
public class LivingAttackEvent extends LivingEvent {
  private final DamageSource source;
  private final float amount;

  public LivingAttackEvent(LivingEntity entity, DamageSource source, float amount) {
    super(entity);
    this.source = source;
    this.amount = amount;
  }

  public DamageSource getSource() {
    return source;
  }

  public float getAmount() {
    return amount;
  }
}
