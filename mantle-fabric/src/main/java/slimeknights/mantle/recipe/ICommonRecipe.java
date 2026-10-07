package slimeknights.mantle.recipe;

import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeInput;

/**
 * Extension of {@link Recipe} to set some methods that always set.
 * @param <I>  Input type
 */
public interface ICommonRecipe<I extends RecipeInput> extends Recipe<I> {
  /**
   * Gets the ID of this recipe as loaded by the recipe manager, replacing the old {@code getId}.
   * Falls back to a placeholder for recipes not loaded from a manager, such as display copies.
   */
  default net.minecraft.resources.ResourceLocation getId() {
    net.minecraft.resources.ResourceLocation id = RecipeIds.get(this);
    return id != null ? id : net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("mantle", "unknown_recipe");
  }

  @Override
  default ItemStack assemble(I input, HolderLookup.Provider provider) {
    return getResultItem(provider).copy();
  }

  /** @deprecated Means nothing outside crafting tables */
  @Deprecated
  @Override
  default boolean canCraftInDimensions(int width, int height) {
    return true;
  }

  /**
   * Returns true to hide this recipe from the recipe book. Needed until Forge has proper recipe book support.
   * @return  True
   */
  @Override
  default boolean isSpecial() {
    return true;
  }
}
