package slimeknights.mantle.platform.event.entity;

import com.google.common.collect.ImmutableMap;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.DefaultAttributes;
import slimeknights.mantle.platform.event.Event;
import slimeknights.mantle.platform.event.lifecycle.IModBusEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Adds attributes to existing living entity types. Fabric has no equivalent, so additions are collected and applied by
 * {@code DefaultAttributesMixin} when the default attributes for the type are fetched.
 */
public class EntityAttributeModificationEvent extends Event implements IModBusEvent {
  /** Pending additions by type, filled when the event is fired */
  private static final Map<EntityType<? extends LivingEntity>,Map<Holder<Attribute>,Double>> PENDING = new HashMap<>();
  /** Cache of modified suppliers so each type is only rebuilt once */
  private static final Map<EntityType<?>,AttributeSupplier> MODIFIED = new HashMap<>();

  /** Gets all living entity types with default attributes */
  @SuppressWarnings("unchecked")
  public List<EntityType<? extends LivingEntity>> getTypes() {
    List<EntityType<? extends LivingEntity>> types = new ArrayList<>();
    for (EntityType<?> type : BuiltInRegistries.ENTITY_TYPE) {
      if (DefaultAttributes.hasSupplier(type)) {
        types.add((EntityType<? extends LivingEntity>) type);
      }
    }
    return types;
  }

  /** Adds an attribute to the type using its default value */
  public void add(EntityType<? extends LivingEntity> type, Holder<Attribute> attribute) {
    add(type, attribute, attribute.value().getDefaultValue());
  }

  /** Adds an attribute to the type with the given base value */
  public void add(EntityType<? extends LivingEntity> type, Holder<Attribute> attribute, double value) {
    PENDING.computeIfAbsent(type, t -> new HashMap<>()).put(attribute, value);
    MODIFIED.remove(type);
  }

  /** Called by the mixin to apply additions to the supplier of the given type */
  public static AttributeSupplier modify(EntityType<?> type, AttributeSupplier base) {
    Map<Holder<Attribute>,Double> additions = PENDING.get(type);
    if (additions == null || additions.isEmpty()) {
      return base;
    }
    return MODIFIED.computeIfAbsent(type, t -> {
      Map<Holder<Attribute>,AttributeInstance> instances = new HashMap<>(base.instances);
      additions.forEach((attribute, value) -> instances.computeIfAbsent(attribute, a -> {
        AttributeInstance instance = new AttributeInstance(a, changed -> {
          throw new IllegalStateException("Tried to change value for default attribute instance: " + a.getRegisteredName());
        });
        instance.setBaseValue(value);
        return instance;
      }));
      return new AttributeSupplier(ImmutableMap.copyOf(instances));
    });
  }
}
