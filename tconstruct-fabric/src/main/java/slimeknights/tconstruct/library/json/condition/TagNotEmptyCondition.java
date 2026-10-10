package slimeknights.tconstruct.library.json.condition;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import slimeknights.mantle.recipe.condition.TagFilledCondition;
import slimeknights.tconstruct.TConstruct;

/** Legacy ID of {@link TagFilledCondition} still used by the books, only reading is supported. */
public final class TagNotEmptyCondition {
  private TagNotEmptyCondition() {}

  public static final ResourceLocation ID = TConstruct.getResource("tag_not_empty");
  public static final MapCodec<TagFilledCondition<?>> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
    ResourceLocation.CODEC.fieldOf("registry").forGetter(TagNotEmptyCondition::unsupported),
    ResourceLocation.CODEC.fieldOf("tag").forGetter(TagNotEmptyCondition::unsupported)
  ).apply(instance, (registry, tag) -> createCondition(registry, tag)));

  private static ResourceLocation unsupported(TagFilledCondition<?> condition) {
    throw new UnsupportedOperationException("Only reading the legacy tag_not_empty condition is supported");
  }

  private static <T> TagFilledCondition<T> createCondition(ResourceLocation registry, ResourceLocation tag) {
    return new TagFilledCondition<>(TagKey.create(ResourceKey.<T>createRegistryKey(registry), tag));
  }
}
