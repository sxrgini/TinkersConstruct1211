package slimeknights.mantle.platform.event.entity;

import net.minecraft.world.entity.Entity;
import slimeknights.mantle.platform.event.EntityEvent;
import slimeknights.mantle.platform.event.Event.Cancelable;

/** Fired when an entity teleports */
@Cancelable
public class EntityTeleportEvent extends EntityEvent {
  private double targetX;
  private double targetY;
  private double targetZ;

  public EntityTeleportEvent(Entity entity, double targetX, double targetY, double targetZ) {
    super(entity);
    this.targetX = targetX;
    this.targetY = targetY;
    this.targetZ = targetZ;
  }

  public double getTargetX() {
    return targetX;
  }

  public void setTargetX(double targetX) {
    this.targetX = targetX;
  }

  public double getTargetY() {
    return targetY;
  }

  public void setTargetY(double targetY) {
    this.targetY = targetY;
  }

  public double getTargetZ() {
    return targetZ;
  }

  public void setTargetZ(double targetZ) {
    this.targetZ = targetZ;
  }

  /** Teleporting with an ender pearl */
  public static class EnderPearl extends EntityTeleportEvent {
    private final net.minecraft.server.level.ServerPlayer player;

    public EnderPearl(net.minecraft.server.level.ServerPlayer entity, double targetX, double targetY, double targetZ) {
      super(entity, targetX, targetY, targetZ);
      this.player = entity;
    }

    public net.minecraft.server.level.ServerPlayer getPlayer() {
      return player;
    }
  }

  /** Teleporting with the teleport command */
  public static class TeleportCommand extends EntityTeleportEvent {
    public TeleportCommand(Entity entity, double targetX, double targetY, double targetZ) {
      super(entity, targetX, targetY, targetZ);
    }
  }
}
