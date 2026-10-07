package slimeknights.tconstruct.library.recipe.casting;

import slimeknights.mantle.platform.fluid.FluidStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.material.Fluid;
import slimeknights.mantle.recipe.input.SingleItemInput;

import javax.annotation.Nullable;

/**
 * Inventory containing a single item and a fluid
 */
public interface ICastingContainer extends SingleItemInput {
  /**
   * Gets the contained fluid in this inventory
   * @return  Contained fluid
   */
  FluidStack getFluidStack();

  /** {@return the contained fluid} */
  default Fluid getFluid() {
    return getFluidStack().getFluid();
  }

}
