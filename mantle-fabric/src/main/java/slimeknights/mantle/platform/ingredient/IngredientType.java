package slimeknights.mantle.platform.ingredient;

import com.mojang.serialization.MapCodec;
import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredientSerializer;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

/**
 * Serializer type for a custom item ingredient, replacing NeoForge's {@code IngredientType}. Call {@link #register(ResourceLocation)} to make it known to Fabric.
 */
public class IngredientType<T extends ICustomIngredient> implements CustomIngredientSerializer<T> {
  private final MapCodec<T> codec;
  private final StreamCodec<RegistryFriendlyByteBuf,T> streamCodec;
  private ResourceLocation id;

  public IngredientType(MapCodec<T> codec, StreamCodec<RegistryFriendlyByteBuf,T> streamCodec) {
    this.codec = codec;
    this.streamCodec = streamCodec;
  }

  /** Registers this ingredient type with the given ID, returning this for chaining */
  public IngredientType<T> register(ResourceLocation id) {
    this.id = id;
    CustomIngredientSerializer.register(this);
    return this;
  }

  @Override
  public ResourceLocation getIdentifier() {
    if (id == null) {
      throw new IllegalStateException("Ingredient type has not been registered");
    }
    return id;
  }

  @Override
  public MapCodec<T> getCodec(boolean allowEmpty) {
    return codec;
  }

  @Override
  public StreamCodec<RegistryFriendlyByteBuf,T> getPacketCodec() {
    return streamCodec;
  }
}
