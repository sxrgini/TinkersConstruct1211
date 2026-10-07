package slimeknights.mantle.recipe.data;

import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import slimeknights.mantle.platform.condition.ICondition;

import javax.annotation.Nullable;

/** Recipe output supporting load conditions, replacing NeoForge's extension of {@link RecipeOutput} */
public interface ConditionalRecipeOutput extends RecipeOutput {
  /** Accepts a recipe that only loads when all passed conditions are true */
  void accept(ResourceLocation id, Recipe<?> recipe, @Nullable AdvancementHolder advancement, ICondition... conditions);

  @Override
  default void accept(ResourceLocation id, Recipe<?> recipe, @Nullable AdvancementHolder advancement) {
    accept(id, recipe, advancement, new ICondition[0]);
  }

  /** Wraps the given output to add the passed conditions to every recipe */
  static RecipeOutput withConditions(RecipeOutput output, ICondition... conditions) {
    if (conditions.length == 0) {
      return output;
    }
    return new ConditionalRecipeOutput() {
      @Override
      public void accept(ResourceLocation id, Recipe<?> recipe, @Nullable AdvancementHolder advancement, ICondition... extra) {
        ICondition[] all = new ICondition[conditions.length + extra.length];
        System.arraycopy(conditions, 0, all, 0, conditions.length);
        System.arraycopy(extra, 0, all, conditions.length, extra.length);
        if (output instanceof ConditionalRecipeOutput conditional) {
          conditional.accept(id, recipe, advancement, all);
        } else {
          output.accept(id, recipe, advancement);
        }
      }

      @Override
      public net.minecraft.advancements.Advancement.Builder advancement() {
        return output.advancement();
      }
    };
  }
}
