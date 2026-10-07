package slimeknights.mantle.platform.fluid.crafting;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.material.Fluid;
import slimeknights.mantle.platform.fluid.FluidStack;
import slimeknights.mantle.platform.fluid.FluidType;

import java.util.stream.Stream;

/** Ingredient matching a single fluid */
public final class SingleFluidIngredient extends FluidIngredient {
  public static final MapCodec<SingleFluidIngredient> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
    FluidStack.FLUID_NON_EMPTY_CODEC.fieldOf("fluid").forGetter(SingleFluidIngredient::fluid)
  ).apply(i, SingleFluidIngredient::new));
  public static final StreamCodec<RegistryFriendlyByteBuf,SingleFluidIngredient> STREAM_CODEC = ByteBufCodecs.registry(net.minecraft.core.registries.Registries.FLUID)
    .map(SingleFluidIngredient::new, SingleFluidIngredient::fluid).mapStream(buf -> buf);
  static final FluidIngredientType<SingleFluidIngredient> TYPE = new FluidIngredientType<>(CODEC, STREAM_CODEC);

  private final Fluid fluid;

  public SingleFluidIngredient(Fluid fluid) {
    this.fluid = fluid;
  }

  public Fluid fluid() {
    return fluid;
  }

  @Override
  public FluidIngredientType<?> getType() {
    return TYPE;
  }

  @Override
  public boolean test(FluidStack stack) {
    return stack.is(fluid);
  }

  @Override
  public boolean isSimple() {
    return true;
  }

  @Override
  protected Stream<FluidStack> generateStacks() {
    return Stream.of(new FluidStack(fluid, FluidType.BUCKET_VOLUME));
  }

  @Override
  public boolean equals(Object obj) {
    return obj instanceof SingleFluidIngredient other && other.fluid == fluid;
  }

  @Override
  public int hashCode() {
    return BuiltInRegistries.FLUID.getKey(fluid).hashCode();
  }
}
