package slimeknights.mantle.platform.event;

import net.minecraft.world.entity.Entity;

/** Base for events involving an entity */
public class EntityEvent extends Event {
  private final Entity entity;

  public EntityEvent(Entity entity) {
    this.entity = entity;
  }

  public Entity getEntity() {
    return entity;
  }
}
