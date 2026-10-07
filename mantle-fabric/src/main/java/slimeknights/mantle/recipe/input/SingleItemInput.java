package slimeknights.mantle.recipe.input;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;

/** Extension of {@link RecipeInput} with a single item. Extendable unlike {@link net.minecraft.world.item.crafting.SingleRecipeInput} */
public interface SingleItemInput extends RecipeInput {
  /** Gets the item stack */
  default ItemStack getItem() {
    return getStack();
  }

  /** Alias of {@link #getItem()} matching the old container naming, implementors must override one of the two */
  default ItemStack getStack() {
    return getItem();
  }

  @Override
  default int size() {
    return 1;
  }

  @Override
  default ItemStack getItem(int i) {
    return i == 0 ? getItem() : ItemStack.EMPTY;
  }

  @Override
  default boolean isEmpty() {
    return getItem().isEmpty();
  }
}
