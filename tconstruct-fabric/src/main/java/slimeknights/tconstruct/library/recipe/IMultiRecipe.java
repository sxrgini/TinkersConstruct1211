package slimeknights.tconstruct.library.recipe;

import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;
import slimeknights.mantle.recipe.MultiNamedRecipe;
import slimeknights.mantle.recipe.NamedRecipe;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for dynamic recipes that return a list of display recipes without names. Names are generated from the base recipe ID and index.
 * @param <T>  Recipe type for the return
 */
public interface IMultiRecipe<T> extends MultiNamedRecipe<T> {
  /**
   * Gets a list of recipes for display
   * @param provider  Registry access instance
   * @return  List of recipes
   */
  List<T> getRecipes(HolderLookup.Provider provider);

  @Override
  default List<NamedRecipe<T>> getRecipes(ResourceLocation id, HolderLookup.Provider provider) {
    List<T> recipes = getRecipes(provider);
    List<NamedRecipe<T>> named = new ArrayList<>(recipes.size());
    for (int i = 0; i < recipes.size(); i++) {
      named.add(NamedRecipe.of(id.withSuffix("/" + i), recipes.get(i)));
    }
    return named;
  }
}
