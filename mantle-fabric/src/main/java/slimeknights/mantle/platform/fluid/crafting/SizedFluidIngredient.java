package slimeknights.mantle.platform.fluid.crafting;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;
import slimeknights.mantle.platform.fluid.FluidStack;

import java.util.List;

/** Fluid ingredient with an amount, replacing NeoForge's {@code SizedFluidIngredient}. */
public record SizedFluidIngredient(FluidIngredient ingredient, int amount) {
  /** Codec flattening the ingredient fields and the amount into one object */
  public static final Codec<SizedFluidIngredient> FLAT_CODEC = Codec.mapPair(
    FluidIngredient.MAP_CODEC_NON_EMPTY,
    Codec.intRange(1, Integer.MAX_VALUE).fieldOf("amount")
  ).codec().xmap(pair -> new SizedFluidIngredient(pair.getFirst(), pair.getSecond()), sized -> Pair.of(sized.ingredient, sized.amount));

  public static final StreamCodec<RegistryFriendlyByteBuf,SizedFluidIngredient> STREAM_CODEC = StreamCodec.composite(
    FluidIngredient.STREAM_CODEC, SizedFluidIngredient::ingredient,
    ByteBufCodecs.VAR_INT, SizedFluidIngredient::amount,
    SizedFluidIngredient::new);

  /** Creates a sized ingredient for a single fluid */
  public static SizedFluidIngredient of(Fluid fluid, int amount) {
    return new SizedFluidIngredient(FluidIngredient.single(fluid), amount);
  }

  /** Creates a sized ingredient for a tag */
  public static SizedFluidIngredient of(TagKey<Fluid> tag, int amount) {
    return new SizedFluidIngredient(FluidIngredient.tag(tag), amount);
  }

  /** Creates a sized ingredient from a stack */
  public static SizedFluidIngredient of(FluidStack stack) {
    return new SizedFluidIngredient(FluidIngredient.single(stack), stack.getAmount());
  }

  /** Checks if the stack matches the ingredient and has enough fluid */
  public boolean test(FluidStack stack) {
    return ingredient.test(stack) && stack.getAmount() >= amount;
  }

  /** Gets all stacks matching this ingredient, with the correct amount */
  public List<FluidStack> getFluids() {
    return ingredient.getStacks().stream().map(stack -> stack.copyWithAmount(amount)).toList();
  }
}
