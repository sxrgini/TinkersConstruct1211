package slimeknights.tconstruct.library.data.recipe;

import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import slimeknights.mantle.platform.condition.ICondition;
import slimeknights.mantle.recipe.data.ConditionalRecipeOutput;

import javax.annotation.Nullable;

/** Helper to add data components to the result of vanilla recipes, as the builders do not support them. */
public class CraftingNBTWrapper {
  private CraftingNBTWrapper() {}

  /** Creates a wrapped output, applying the given components to the result of shaped and shapeless recipes */
  public static RecipeOutput wrap(RecipeOutput base, DataComponentPatch components) {
    return new ConditionalRecipeOutput() {
      @Override
      public void accept(ResourceLocation id, Recipe<?> recipe, @Nullable AdvancementHolder advancement, ICondition... conditions) {
        if (recipe instanceof ShapedRecipe shaped) {
          shaped.result.applyComponents(components);
        } else if (recipe instanceof ShapelessRecipe shapeless) {
          shapeless.result.applyComponents(components);
        }
        if (base instanceof ConditionalRecipeOutput conditional) {
          conditional.accept(id, recipe, advancement, conditions);
        } else {
          base.accept(id, recipe, advancement);
        }
      }

      @Override
      public Advancement.Builder advancement() {
        return base.advancement();
      }
    };
  }
}
