package slimeknights.mantle.platform.event.entity;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import slimeknights.mantle.platform.event.Event;
import slimeknights.mantle.platform.event.lifecycle.IModBusEvent;

/** Registers default attributes for new living entity types, backed by Fabric's default attribute registry */
public class EntityAttributeCreationEvent extends Event implements IModBusEvent {
  public void put(EntityType<? extends LivingEntity> type, AttributeSupplier attributes) {
    FabricDefaultAttributeRegistry.register(type, attributes);
  }
}
