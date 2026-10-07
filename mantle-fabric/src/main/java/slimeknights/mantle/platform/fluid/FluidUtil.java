package slimeknights.mantle.platform.fluid;

import net.minecraft.world.item.ItemStack;
import slimeknights.mantle.platform.capability.Caps;
import slimeknights.mantle.platform.capability.Capabilities;
import slimeknights.mantle.platform.capability.FluidHandlers;
import slimeknights.mantle.platform.capability.LazyOptional;

import java.util.Optional;

/** Fluid helpers, replacing the parts of Forge's {@code FluidUtil} that Tinkers' Construct uses */
public final class FluidUtil {
  private FluidUtil() {}

  /** Gets a fluid handler for the stack, checking capability providers on the item first then the Fabric Transfer API */
  public static LazyOptional<IFluidHandlerItem> getFluidHandler(ItemStack stack) {
    LazyOptional<IFluidHandlerItem> provided = Caps.get(stack, Capabilities.FLUID_HANDLER_ITEM);
    if (provided.isPresent()) {
      return provided;
    }
    IFluidHandlerItem fabric = FluidHandlers.getItem(stack);
    return fabric == null ? LazyOptional.empty() : LazyOptional.of(() -> fabric);
  }

  /** Gets the fluid contained in the first tank of the stack */
  public static Optional<FluidStack> getFluidContained(ItemStack container) {
    if (container.isEmpty()) {
      return Optional.empty();
    }
    return getFluidHandler(container.copyWithCount(1)).resolve().map(handler -> handler.drain(Integer.MAX_VALUE, IFluidHandler.FluidAction.SIMULATE)).filter(stack -> !stack.isEmpty());
  }
}
