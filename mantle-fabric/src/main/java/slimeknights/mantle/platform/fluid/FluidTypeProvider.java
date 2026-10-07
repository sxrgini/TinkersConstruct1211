package slimeknights.mantle.platform.fluid;

/** Implemented by fluids that know their {@link FluidType}. Vanilla fluids use defaults in {@link FluidTypes}. */
public interface FluidTypeProvider {
  /** Gets the fluid type of this fluid */
  FluidType getFluidType();
}
