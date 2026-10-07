package slimeknights.mantle.platform.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

/** Holder for an item. */
public class DeferredItem<T extends Item> extends DeferredHolder<Item,T> implements ItemLike {
  protected DeferredItem(ResourceKey<Item> key) {
    super(key);
  }

  /** Creates a holder for the given item key */
  public static <T extends Item> DeferredItem<T> createItem(ResourceKey<Item> key) {
    return new DeferredItem<>(key);
  }

  @Override
  public T asItem() {
    return get();
  }

  /** Creates a stack of this item */
  public ItemStack toStack() {
    return new ItemStack(this.get());
  }
}
