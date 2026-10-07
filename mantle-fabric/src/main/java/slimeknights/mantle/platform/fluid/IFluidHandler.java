package slimeknights.mantle.platform.fluid;

import net.minecraft.world.level.material.Fluid;

/** Fabric-side mirror of NeoForge's {@code IFluidHandler}, amounts in mB. Bridged to the Fabric Transfer API elsewhere. */
public interface IFluidHandler {
  /** Whether an operation is simulated or executed */
  enum FluidAction {
    EXECUTE,
    SIMULATE;

    public boolean execute() {
      return this == EXECUTE;
    }

    public boolean simulate() {
      return this == SIMULATE;
    }
  }

  int getTanks();

  FluidStack getFluidInTank(int tank);

  int getTankCapacity(int tank);

  boolean isFluidValid(int tank, FluidStack stack);

  int fill(FluidStack resource, FluidAction action);

  FluidStack drain(FluidStack resource, FluidAction action);

  FluidStack drain(int maxDrain, FluidAction action);

  /** Convenience for checking if a fluid is accepted */
  default boolean isFluidValid(int tank, Fluid fluid) {
    return isFluidValid(tank, new FluidStack(fluid, 1));
  }
}
