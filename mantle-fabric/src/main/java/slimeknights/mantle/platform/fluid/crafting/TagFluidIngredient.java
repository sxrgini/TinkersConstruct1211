package slimeknights.mantle.platform.fluid.crafting;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;
import slimeknights.mantle.platform.fluid.FluidStack;
import slimeknights.mantle.platform.fluid.FluidType;

import java.util.stream.Stream;

/** Ingredient matching a fluid tag */
public final class TagFluidIngredient extends FluidIngredient {
  public static final MapCodec<TagFluidIngredient> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
    TagKey.codec(Registries.FLUID).fieldOf("tag").forGetter(TagFluidIngredient::tag)
  ).apply(i, TagFluidIngredient::new));
  public static final StreamCodec<RegistryFriendlyByteBuf,TagFluidIngredient> STREAM_CODEC = ResourceLocation.STREAM_CODEC
    .map(id -> new TagFluidIngredient(TagKey.create(Registries.FLUID, id)), i -> i.tag.location())
    .mapStream(buf -> buf);
  static final FluidIngredientType<TagFluidIngredient> TYPE = new FluidIngredientType<>(CODEC, STREAM_CODEC);

  private final TagKey<Fluid> tag;

  public TagFluidIngredient(TagKey<Fluid> tag) {
    this.tag = tag;
  }

  public TagKey<Fluid> tag() {
    return tag;
  }

  @Override
  public FluidIngredientType<?> getType() {
    return TYPE;
  }

  @Override
  public boolean test(FluidStack stack) {
    return stack.is(tag);
  }

  @Override
  public boolean isSimple() {
    return true;
  }

  @Override
  protected Stream<FluidStack> generateStacks() {
    return BuiltInRegistries.FLUID.getTag(tag).stream()
      .flatMap(named -> named.stream())
      .map(Holder::value)
      .map(fluid -> new FluidStack(fluid, FluidType.BUCKET_VOLUME));
  }

  @Override
  public boolean equals(Object obj) {
    return obj instanceof TagFluidIngredient other && other.tag.equals(tag);
  }

  @Override
  public int hashCode() {
    return tag.hashCode();
  }
}
