package slimeknights.mantle.platform.condition;

import net.minecraft.resources.ResourceLocation;

/** Helper methods to build conditions, replacing NeoForge's {@code IConditionBuilder} */
public interface IConditionBuilder {
  /** Wraps the output to add the given conditions to all recipes */
  default net.minecraft.data.recipes.RecipeOutput withCondition(net.minecraft.data.recipes.RecipeOutput output, ICondition... conditions) {
    return slimeknights.mantle.recipe.data.ConditionalRecipeOutput.withConditions(output, conditions);
  }

  default ICondition and(ICondition... values) {
    return new AndCondition(java.util.List.of(values));
  }

  default ICondition or(ICondition... values) {
    return new OrCondition(java.util.List.of(values));
  }

  default ICondition not(ICondition value) {
    return new NotCondition(value);
  }

  default ICondition modLoaded(String modId) {
    return new ModLoadedCondition(modId);
  }

  default ICondition itemExists(String namespace, String path) {
    return new ItemExistsCondition(ResourceLocation.fromNamespaceAndPath(namespace, path));
  }

  default ICondition TRUE() {
    return TrueCondition.INSTANCE;
  }

  default ICondition FALSE() {
    return FalseCondition.INSTANCE;
  }
}
