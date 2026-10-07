package slimeknights.mantle.platform.fluid.crafting;

import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

/** Serializer type for fluid ingredients, replacing NeoForge's {@code FluidIngredientType}. Call {@link #register(ResourceLocation)} to make it known. */
public final class FluidIngredientType<T extends FluidIngredient> {
  private final MapCodec<T> codec;
  private final StreamCodec<RegistryFriendlyByteBuf,T> streamCodec;
  private ResourceLocation id;

  public FluidIngredientType(MapCodec<T> codec, StreamCodec<RegistryFriendlyByteBuf,T> streamCodec) {
    this.codec = codec;
    this.streamCodec = streamCodec;
  }

  public MapCodec<T> codec() {
    return codec;
  }

  public StreamCodec<RegistryFriendlyByteBuf,T> streamCodec() {
    return streamCodec;
  }

  public ResourceLocation id() {
    if (id == null) {
      throw new IllegalStateException("Fluid ingredient type has not been registered");
    }
    return id;
  }

  /** Registers this type with the given ID, returning this for chaining */
  public FluidIngredientType<T> register(ResourceLocation id) {
    this.id = id;
    FluidIngredient.registerType(id, this);
    return this;
  }
}
