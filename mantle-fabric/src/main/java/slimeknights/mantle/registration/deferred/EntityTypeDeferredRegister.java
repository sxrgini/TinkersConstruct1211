package slimeknights.mantle.registration.deferred;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.Item;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.SpawnEggItem;
import slimeknights.mantle.platform.registry.DeferredHolder;
import slimeknights.mantle.platform.registry.DeferredRegister;
import slimeknights.mantle.registration.object.EntityObject;

import java.util.function.Supplier;

/**
 * Deferred register for an entity, building the type from a builder instance and adding an egg
 */
@SuppressWarnings("unused")
public class EntityTypeDeferredRegister extends DeferredRegister<EntityType<?>> {
  private final DeferredRegister<Item> itemRegistry;

  public EntityTypeDeferredRegister(String modID, DeferredRegister<Item> itemRegistry) {
    super(Registries.ENTITY_TYPE, modID);
    this.itemRegistry = itemRegistry;
  }

  public EntityTypeDeferredRegister(String modID) {
    this(modID, DeferredRegister.create(Registries.ITEM, modID));
  }

  @Override
  public void register() {
    super.register();
    itemRegistry.register();
  }

  /**
   * Registers an entity type for the given entity type builder with no spawn egg.
   * @param name  Entity name
   * @param sup   Entity builder instance
   * @param <T>   Entity class type
   * @return  Entity registry object
   */
  public <T extends Entity> DeferredHolder<EntityType<?>, EntityType<T>> registerNoEgg(String name, Supplier<EntityType.Builder<T>> sup) {
    return register(name, id -> sup.get().build(id.toString()));
  }

  /**
   * Registers a entity type for the given entity type builder, and registers a spawn egg for it
   * @param name       Entity name
   * @param sup        Entity builder instance
   * @param primary    Primary egg color
   * @param secondary  Secondary egg color
   * @param <T>   Entity class type
   * @return  Entity registry object
   */
  public <T extends Mob> EntityObject<T> registerWithEgg(String name, Supplier<EntityType.Builder<T>> sup, int primary, int secondary) {
    DeferredHolder<EntityType<?>, EntityType<T>> object = registerNoEgg(name, sup);
    return new EntityObject<>(object, itemRegistry.register(name + "_spawn_egg", () -> new SpawnEggItem((EntityType<? extends Mob>) object.get(), primary, secondary, new Item.Properties())));
  }
}
