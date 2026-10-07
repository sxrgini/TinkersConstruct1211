package slimeknights.mantle.recipe.helper;

import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import slimeknights.mantle.recipe.MultiNamedRecipe;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/** Helpers for fetching recipes by class from the recipe manager */
public final class RecipeHelper {
  private RecipeHelper() {}

  /** Gets a recipe by ID, filtered to the given class */
  public static <T> Optional<T> getRecipe(RecipeManager manager, ResourceLocation id, Class<T> clazz) {
    return manager.byKey(id).map(RecipeHolder::value).filter(clazz::isInstance).map(clazz::cast);
  }

  /** Gets all recipes of the given type that are instances of the given class */
  public static <T, I extends RecipeInput, R extends Recipe<I>> List<T> getRecipes(RecipeManager manager, RecipeType<R> type, Class<T> clazz) {
    return manager.byType(type).stream().map(RecipeHolder::value).filter(clazz::isInstance).map(clazz::cast).toList();
  }

  /** Gets all recipes of the given type including those expanded from multi recipes, for display */
  public static <T, I extends RecipeInput, R extends Recipe<I>> List<T> getJEIRecipes(HolderLookup.Provider provider, RecipeManager manager, RecipeType<R> type, Class<T> clazz) {
    return getJEIRecipeStream(provider, manager, type, clazz).toList();
  }

  /** Gets all recipes of the given type including those expanded from multi recipes, for display */
  public static <T, I extends RecipeInput, R extends Recipe<I>> Stream<T> getJEIRecipeStream(HolderLookup.Provider provider, RecipeManager manager, RecipeType<R> type, Class<T> clazz) {
    return MultiNamedRecipe.streamRecipes(provider, manager, type, clazz);
  }
}
