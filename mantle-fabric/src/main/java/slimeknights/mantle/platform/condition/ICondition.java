package slimeknights.mantle.platform.condition;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;

import java.util.Collection;
import java.util.Map;

/** Mantle's own load condition system, replacing NeoForge's {@code ICondition} */
public interface ICondition {
  /** Codec dispatching on the {@code type} field using {@link ConditionRegistry} */
  Codec<ICondition> CODEC = ConditionRegistry.CODEC;

  /** Gets the codec used to serialize this condition */
  MapCodec<? extends ICondition> codec();

  /** Tests this condition in the given context */
  boolean test(IContext context);

  /** Context for conditions, mainly to look up tags */
  interface IContext {
    /** Context where all tags are empty */
    IContext EMPTY = new IContext() {
      @Override
      public <T> Collection<Holder<T>> getTag(TagKey<T> key) {
        return java.util.List.of();
      }

      @Override
      public <T> Map<ResourceLocation,Collection<Holder<T>>> getAllTags(ResourceKey<? extends Registry<T>> key) {
        return Map.of();
      }
    };
    /** Context used when tags are not yet loaded, returning an empty tag */
    IContext TAGS_INVALID = EMPTY;

    /** Gets the contents of a tag */
    <T> Collection<Holder<T>> getTag(TagKey<T> key);

    /** Gets all tags in a registry */
    <T> Map<ResourceLocation,Collection<Holder<T>>> getAllTags(ResourceKey<? extends Registry<T>> key);
  }
}
