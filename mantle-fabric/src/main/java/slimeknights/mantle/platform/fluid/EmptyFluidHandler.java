package slimeknights.mantle.platform.fluid;

/** Fluid handler with no tanks, replacing Forge's {@code EmptyFluidHandler} */
public final class EmptyFluidHandler implements IFluidHandlerItem {
  public static final EmptyFluidHandler INSTANCE = new EmptyFluidHandler();

  private EmptyFluidHandler() {}

  @Override
  public int getTanks() {
    return 1;
  }

  @Override
  public FluidStack getFluidInTank(int tank) {
    return FluidStack.EMPTY;
  }

  @Override
  public int getTankCapacity(int tank) {
    return 0;
  }

  @Override
  public boolean isFluidValid(int tank, FluidStack stack) {
    return true;
  }

  @Override
  public int fill(FluidStack resource, FluidAction action) {
    return 0;
  }

  @Override
  public FluidStack drain(FluidStack resource, FluidAction action) {
    return FluidStack.EMPTY;
  }

  @Override
  public FluidStack drain(int maxDrain, FluidAction action) {
    return FluidStack.EMPTY;
  }

  @Override
  public net.minecraft.world.item.ItemStack getContainer() {
    return net.minecraft.world.item.ItemStack.EMPTY;
  }
}
