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
public class LivingExperienceDropEvent extends LivingEvent {
  private final Player attackingPlayer;
  private final int originalExperience;
  private int droppedExperience;

  public LivingExperienceDropEvent(LivingEntity entity, Player attackingPlayer, int originalExperience, int droppedExperience) {
    super(entity);
    this.attackingPlayer = attackingPlayer;
    this.originalExperience = originalExperience;
    this.droppedExperience = droppedExperience;
  }

  public Player getAttackingPlayer() {
    return attackingPlayer;
  }

  public int getOriginalExperience() {
    return originalExperience;
  }

  public int getDroppedExperience() {
    return droppedExperience;
  }

  public void setDroppedExperience(int droppedExperience) {
    this.droppedExperience = droppedExperience;
  }

}
