package slimeknights.tconstruct.common.data;

import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeOutput;
import slimeknights.mantle.recipe.data.IRecipeHelper;
import slimeknights.tconstruct.TConstruct;

import java.util.function.Consumer;

/**
 * Shared logic for each module's recipe provider
 */
public abstract class BaseRecipeProvider extends slimeknights.mantle.recipe.data.MantleRecipeProvider implements IRecipeHelper, slimeknights.mantle.platform.condition.IConditionBuilder {
  public BaseRecipeProvider(PackOutput generator, java.util.concurrent.CompletableFuture<net.minecraft.core.HolderLookup.Provider> registries) {
    super(generator, registries);
    TConstruct.sealTinkersClass(this, "BaseRecipeProvider", "BaseRecipeProvider is trivial to recreate and directly extending can lead to addon recipes polluting our namespace.");
  }

  @Override
  public abstract void buildRecipes(RecipeOutput consumer);

  @Override
  public abstract String getName();

  @Override
  public String getModId() {
    return TConstruct.MOD_ID;
  }
}
