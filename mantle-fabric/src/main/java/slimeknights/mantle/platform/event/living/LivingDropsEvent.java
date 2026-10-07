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
public class LivingDropsEvent extends LivingEvent {
  private final DamageSource source;
  private final Collection<ItemEntity> drops;
  private final int lootingLevel;
  private final boolean recentlyHit;

  public LivingDropsEvent(LivingEntity entity, DamageSource source, Collection<ItemEntity> drops, int lootingLevel, boolean recentlyHit) {
    super(entity);
    this.source = source;
    this.drops = drops;
    this.lootingLevel = lootingLevel;
    this.recentlyHit = recentlyHit;
  }

  public DamageSource getSource() {
    return source;
  }

  public Collection<ItemEntity> getDrops() {
    return drops;
  }

  public int getLootingLevel() {
    return lootingLevel;
  }


  public boolean isRecentlyHit() {
    return recentlyHit;
  }
}
