package slimeknights.mantle.recipe.data;

import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import slimeknights.mantle.platform.condition.ICondition;

import java.util.ArrayList;
import java.util.List;

/**
 * Builder to wrap a {@link RecipeOutput} adding load conditions to every recipe passed through.
 * The 1.20 version could also swap the recipe serializer, which is not possible with recipe objects so {@link #wrap(ResourceLocation)} only keeps the ID for reference.
 */
public class ConsumerWrapperBuilder {
  private final List<ICondition> conditions = new ArrayList<>();
  /** Serializer ID of the original, unused */
  private final ResourceLocation serializer;

  private ConsumerWrapperBuilder(ResourceLocation serializer) {
    this.serializer = serializer;
  }

  /** Creates a builder only adding conditions */
  public static ConsumerWrapperBuilder wrap() {
    return new ConsumerWrapperBuilder(null);
  }

  /** Creates a builder for a recipe of another type, the type is ignored and only conditions are applied */
  public static ConsumerWrapperBuilder wrap(ResourceLocation serializer) {
    return new ConsumerWrapperBuilder(serializer);
  }

  /** Adds a condition to the builder */
  public ConsumerWrapperBuilder addCondition(ICondition condition) {
    conditions.add(condition);
    return this;
  }

  /** Builds the wrapped output */
  public RecipeOutput build(RecipeOutput output) {
    return ConditionalRecipeOutput.withConditions(output, conditions.toArray(new ICondition[0]));
  }
}
