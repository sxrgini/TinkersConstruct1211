package slimeknights.mantle.platform.condition;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

/** Registry of condition codecs */
public final class ConditionRegistry {
  private static final Map<ResourceLocation,MapCodec<? extends ICondition>> BY_ID = new HashMap<>();
  private static final Map<MapCodec<? extends ICondition>,ResourceLocation> BY_CODEC = new HashMap<>();

  private static final Codec<MapCodec<? extends ICondition>> TYPE_CODEC = ResourceLocation.CODEC.flatXmap(
    id -> {
      MapCodec<? extends ICondition> codec = BY_ID.get(id);
      return codec != null ? DataResult.success(codec) : DataResult.error(() -> "Unknown condition type " + id);
    },
    codec -> {
      ResourceLocation id = BY_CODEC.get(codec);
      return id != null ? DataResult.success(id) : DataResult.error(() -> "Unregistered condition codec " + codec);
    });

  static final Codec<ICondition> CODEC = TYPE_CODEC.dispatch("type", ICondition::codec, codec -> codec.cast());

  private ConditionRegistry() {}

  /** Registers a new condition codec */
  public static void register(ResourceLocation id, MapCodec<? extends ICondition> codec) {
    if (BY_ID.putIfAbsent(id, codec) != null) {
      throw new IllegalArgumentException("Duplicate condition " + id);
    }
    BY_CODEC.put(codec, id);
  }

  static {
    register(ResourceLocation.withDefaultNamespace("true"), TrueCondition.CODEC);
    register(ResourceLocation.withDefaultNamespace("false"), FalseCondition.CODEC);
    register(ResourceLocation.withDefaultNamespace("not"), NotCondition.CODEC);
  }
}
