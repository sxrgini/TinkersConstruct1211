package slimeknights.mantle.platform.fluid;

import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import slimeknights.mantle.Mantle;

/** Registry and lookup for fluid types */
public final class FluidTypes {
  /** Registry key for fluid types */
  public static final ResourceKey<Registry<FluidType>> KEY = ResourceKey.createRegistryKey(Mantle.getResource("fluid_type"));
  /** Registry of fluid types */
  public static final Registry<FluidType> REGISTRY = FabricRegistryBuilder.createSimple(KEY).buildAndRegister();

  public static final FluidType WATER = new FluidType(FluidType.Properties.create().descriptionId("block.minecraft.water").density(1000).viscosity(1000).temperature(300)
    .sound(SoundAction.BUCKET_FILL, net.minecraft.sounds.SoundEvents.BUCKET_FILL).sound(SoundAction.BUCKET_EMPTY, net.minecraft.sounds.SoundEvents.BUCKET_EMPTY));
  public static final FluidType LAVA = new FluidType(FluidType.Properties.create().descriptionId("block.minecraft.lava").density(3000).viscosity(6000).temperature(1300).lightLevel(15)
    .sound(SoundAction.BUCKET_FILL, net.minecraft.sounds.SoundEvents.BUCKET_FILL_LAVA).sound(SoundAction.BUCKET_EMPTY, net.minecraft.sounds.SoundEvents.BUCKET_EMPTY_LAVA));
  /** Type used for fluids that do not declare one */
  public static final FluidType DEFAULT = new FluidType(FluidType.Properties.create());
  public static final FluidType EMPTY = new FluidType(FluidType.Properties.create().density(0).viscosity(0).temperature(0));

  private FluidTypes() {}

  /** Gets the fluid type for the given fluid */
  public static FluidType of(Fluid fluid) {
    if (fluid instanceof FluidTypeProvider provider) {
      return provider.getFluidType();
    }
    if (fluid == Fluids.EMPTY) {
      return EMPTY;
    }
    if (fluid.isSame(Fluids.WATER)) {
      return WATER;
    }
    if (fluid.isSame(Fluids.LAVA)) {
      return LAVA;
    }
    return DEFAULT;
  }
}
