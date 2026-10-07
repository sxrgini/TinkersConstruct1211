package slimeknights.tconstruct.library.recipe.ingredient;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;
import slimeknights.mantle.data.loadable.Loadables;
import slimeknights.mantle.platform.ingredient.ICustomIngredient;
import slimeknights.mantle.platform.ingredient.IngredientType;
import slimeknights.mantle.util.RegistryHelper;
import slimeknights.tconstruct.TConstruct;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/** Item ingredient matching items with a block form in the given tag */
public class BlockTagIngredient implements ICustomIngredient {
  public static final ResourceLocation ID = TConstruct.getResource("block_tag");
  public static final MapCodec<BlockTagIngredient> CODEC = TagKey.codec(Registries.BLOCK).fieldOf("tag").xmap(BlockTagIngredient::new, i -> i.tag);
  public static final StreamCodec<RegistryFriendlyByteBuf,BlockTagIngredient> STREAM_CODEC = Loadables.BLOCK_TAG.asStreamCodec().map(BlockTagIngredient::new, i -> i.tag);
  public static final IngredientType<BlockTagIngredient> TYPE = new IngredientType<>(CODEC, STREAM_CODEC);

  private final TagKey<Block> tag;
  private Set<Item> matchingItems;

  public BlockTagIngredient(TagKey<Block> tag) {
    this.tag = tag;
  }

  /** Creates an ingredient for the tag */
  public static Ingredient of(TagKey<Block> tag) {
    return new BlockTagIngredient(tag).toVanilla();
  }

  @Override
  public boolean test(ItemStack stack) {
    return getMatchingItems().contains(stack.getItem());
  }

  @Override
  public boolean isSimple() {
    return true;
  }

  /** Gets the ordered matching items set */
  @SuppressWarnings("deprecation")
  private Set<Item> getMatchingItems() {
    if (matchingItems == null) {
      matchingItems = RegistryHelper.getTagValueStream(BuiltInRegistries.BLOCK, tag)
                                    .map(Block::asItem)
                                    .filter(item -> item != Items.AIR)
                                    .collect(Collectors.toCollection(LinkedHashSet::new));
    }
    return matchingItems;
  }

  @Override
  public Stream<ItemStack> getItems() {
    return getMatchingItems().stream().map(ItemStack::new);
  }

  @Override
  public IngredientType<?> getType() {
    return TYPE;
  }
}
