package slimeknights.mantle.recipe.data;

import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import slimeknights.mantle.platform.condition.ICondition;
import slimeknights.mantle.platform.condition.NotCondition;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Replacement for Forge's conditional recipe, which picked the first option with passing conditions.
 * Since recipes cannot hold alternatives in 1.21, each alternative is saved as its own recipe with the conditions of every earlier option inverted.
 */
public class ConditionalRecipeBuilder {
  private final List<ICondition> conditions = new ArrayList<>();
  private final List<Consumer<RecipeOutput>> recipes = new ArrayList<>();
  private final List<ICondition> currentConditions = new ArrayList<>();

  /** Creates a new builder */
  public static ConditionalRecipeBuilder builder() {
    return new ConditionalRecipeBuilder();
  }

  /** Adds a condition to the current option */
  public ConditionalRecipeBuilder addCondition(ICondition condition) {
    currentConditions.add(condition);
    return this;
  }

  /** Finishes the current option with the given recipe */
  public ConditionalRecipeBuilder addRecipe(Consumer<RecipeOutput> recipe) {
    if (currentConditions.isEmpty()) {
      throw new IllegalStateException("Must add at least one condition before adding a recipe");
    }
    recipes.add(recipe);
    // flatten all conditions of this option into a single and
    conditions.add(currentConditions.size() == 1 ? currentConditions.get(0) : new slimeknights.mantle.platform.condition.AndCondition(List.copyOf(currentConditions)));
    currentConditions.clear();
    return this;
  }

  /** No-op, advancements are not generated for conditional recipes */
  public ConditionalRecipeBuilder generateAdvancement() {
    return this;
  }

  /** Saves all options under the given ID, later options get a numbered suffix */
  public void build(RecipeOutput output, ResourceLocation id) {
    for (int i = 0; i < recipes.size(); i++) {
      ICondition[] all = new ICondition[i + 1];
      all[0] = conditions.get(i);
      for (int j = 0; j < i; j++) {
        all[j + 1] = new NotCondition(conditions.get(j));
      }
      ResourceLocation optionId = i == 0 ? id : id.withSuffix("_alt" + i);
      recipes.get(i).accept(new ConditionalRecipeOutput() {
        @Override
        public void accept(ResourceLocation ignored, Recipe<?> recipe, @Nullable AdvancementHolder advancement, ICondition... extra) {
          ICondition[] full = new ICondition[all.length + extra.length];
          System.arraycopy(all, 0, full, 0, all.length);
          System.arraycopy(extra, 0, full, all.length, extra.length);
          if (output instanceof ConditionalRecipeOutput conditional) {
            conditional.accept(optionId, recipe, advancement == null ? null : new AdvancementHolder(optionId.withPrefix("recipes/"), advancement.value()), full);
          } else {
            output.accept(optionId, recipe, advancement);
          }
        }

        @Override
        public Advancement.Builder advancement() {
          return output.advancement();
        }
      });
    }
  }
}
