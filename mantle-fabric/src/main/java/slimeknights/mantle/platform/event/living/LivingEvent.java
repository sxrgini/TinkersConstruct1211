package slimeknights.mantle.platform.event.living;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import slimeknights.mantle.platform.event.Event.Cancelable;
import slimeknights.mantle.platform.event.EntityEvent;

/** Base for events involving a living entity */
public class LivingEvent extends EntityEvent {
  public LivingEvent(LivingEntity entity) {
    super(entity);
  }

  @Override
  public LivingEntity getEntity() {
    return (LivingEntity) super.getEntity();
  }

  /** Fired every tick for living entities */
  @Cancelable
  public static class LivingTickEvent extends LivingEvent {
    public LivingTickEvent(LivingEntity entity) {
      super(entity);
    }
  }

  /** Fired when a living entity jumps */
  public static class LivingJumpEvent extends LivingEvent {
    public LivingJumpEvent(LivingEntity entity) {
      super(entity);
    }
  }

  /** Fired when computing how visible an entity is to another */
  public static class LivingVisibilityEvent extends LivingEvent {
    private double visibilityModifier;
    private final Entity lookingEntity;

    public LivingVisibilityEvent(LivingEntity entity, Entity lookingEntity, double originalMultiplier) {
      super(entity);
      this.visibilityModifier = originalMultiplier;
      this.lookingEntity = lookingEntity;
    }

    public void modifyVisibility(double mod) {
      visibilityModifier *= mod;
    }

    public double getVisibilityModifier() {
      return visibilityModifier;
    }

    public Entity getLookingEntity() {
      return lookingEntity;
    }
  }
}
