package slimeknights.mantle.recipe.data;

import net.minecraft.resources.ResourceLocation;
import slimeknights.mantle.recipe.ingredient.FluidIngredient;

/** Helper to create fluid ingredients matching by name, which do not fail if the fluid is missing */
public class FluidNameIngredient {
  private FluidNameIngredient() {}

  /** Creates an ingredient matching the fluid with the given name */
  public static FluidIngredient of(ResourceLocation name, int amount) {
    return FluidIngredient.ofName(name, amount);
  }
}
