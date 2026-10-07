package slimeknights.mantle.recipe.ingredient;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.material.Fluid;
import slimeknights.mantle.platform.ingredient.ICustomIngredient;
import slimeknights.mantle.platform.ingredient.IngredientType;
import slimeknights.mantle.platform.ingredient.SizedIngredient;
import slimeknights.mantle.platform.fluid.FluidStack;
import slimeknights.mantle.platform.fluid.crafting.FluidIngredient;
import slimeknights.mantle.platform.fluid.crafting.FluidIngredientType;
import slimeknights.mantle.data.loadable.LoadableCodec;
import slimeknights.mantle.data.loadable.StreamableCodec;
import slimeknights.mantle.data.loadable.record.RecordLoadable;

/** Helpers for working with ingredients. Largely bringing back lost but useful behavior from the removed Mantle ingredients. */
public class IngredientHelper {
  private IngredientHelper() {}

  /** Creates a new sized ingredient from the given items with the given size. */
  public static SizedIngredient sized(int size, ItemLike... items) {
    return new SizedIngredient(Ingredient.of(items), size);
  }

  /** Creates a new sized ingredient from the given stack. */
  public static SizedIngredient sized(ItemStack stack) {
    return new SizedIngredient(Ingredient.of(stack), stack.getCount());
  }

  /** Creates an ingredient type from the given loadable. */
  public static <T extends ICustomIngredient> IngredientType<T> itemType(RecordLoadable<T> loadable) {
    return new IngredientType<>(
      MapCodec.assumeMapUnsafe(new LoadableCodec<>(loadable)),
      new StreamableCodec<>(loadable)
    );
  }

  /** Creates an ingredient type from the given loadable. */
  public static <T extends FluidIngredient> FluidIngredientType<T> fluidType(RecordLoadable<T> loadable) {
    return new FluidIngredientType<>(
      MapCodec.assumeMapUnsafe(new LoadableCodec<>(loadable)),
      new StreamableCodec<>(loadable)
    );
  }

  /** Checks if the given fluid is a source (that is, not flowing). */
  public static boolean isSource(Fluid fluid) {
    return fluid.isSource(fluid.defaultFluidState());
  }

  /**
   * Checks if the given fluid stack is a source (that is, not flowing).
   * Used to filter fluids returned by {@link FluidIngredient} and {@link slimeknights.mantle.platform.fluid.crafting.SizedFluidIngredient} in focus linked recipes as JEI hides such fluids.
   */
  public static boolean isSource(FluidStack stack) {
    return !stack.isEmpty() && isSource(stack.getFluid());
  }
}
