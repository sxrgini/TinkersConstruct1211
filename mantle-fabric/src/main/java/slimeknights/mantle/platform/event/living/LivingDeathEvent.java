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
public class LivingDeathEvent extends LivingEvent {
  private final DamageSource source;

  public LivingDeathEvent(LivingEntity entity, DamageSource source) {
    super(entity);
    this.source = source;
  }

  public DamageSource getSource() {
    return source;
  }
}
