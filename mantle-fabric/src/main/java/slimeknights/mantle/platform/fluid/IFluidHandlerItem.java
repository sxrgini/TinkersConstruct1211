package slimeknights.mantle.platform.fluid;

import net.minecraft.world.item.ItemStack;

/** Fluid handler attached to an item stack. */
public interface IFluidHandlerItem extends IFluidHandler {
  /** Gets the container stack, which may change after fill or drain */
  ItemStack getContainer();
}
