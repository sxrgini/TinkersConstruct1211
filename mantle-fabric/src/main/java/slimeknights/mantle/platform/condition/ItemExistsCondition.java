package slimeknights.mantle.platform.condition;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;

/** Condition that is true when an item with the given ID is registered */
public record ItemExistsCondition(ResourceLocation item) implements ICondition {
  public static final MapCodec<ItemExistsCondition> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
    ResourceLocation.CODEC.fieldOf("item").forGetter(ItemExistsCondition::item)
  ).apply(i, ItemExistsCondition::new));

  public ItemExistsCondition(String namespace, String path) {
    this(ResourceLocation.fromNamespaceAndPath(namespace, path));
  }

  @Override
  public MapCodec<? extends ICondition> codec() {
    return CODEC;
  }

  @Override
  public boolean test(IContext context) {
    return BuiltInRegistries.ITEM.containsKey(item);
  }
}
