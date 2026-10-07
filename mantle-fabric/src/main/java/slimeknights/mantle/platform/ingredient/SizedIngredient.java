package slimeknights.mantle.platform.ingredient;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;

import java.util.Arrays;

/** An ingredient with a count, replacing NeoForge's {@code SizedIngredient}. */
public record SizedIngredient(Ingredient ingredient, int count) {
  /** Codec flattening the ingredient fields and the count into one object */
  public static final Codec<SizedIngredient> FLAT_CODEC = Codec.mapPair(
    MapCodec.assumeMapUnsafe(Ingredient.CODEC_NONEMPTY),
    Codec.intRange(1, Integer.MAX_VALUE).optionalFieldOf("count", 1)
  ).codec().xmap(pair -> new SizedIngredient(pair.getFirst(), pair.getSecond()), sized -> Pair.of(sized.ingredient, sized.count));

  public static final StreamCodec<RegistryFriendlyByteBuf,SizedIngredient> STREAM_CODEC = StreamCodec.composite(
    Ingredient.CONTENTS_STREAM_CODEC, SizedIngredient::ingredient,
    ByteBufCodecs.VAR_INT, SizedIngredient::count,
    SizedIngredient::new);

  /** Empty ingredient, matches nothing */
  public static final SizedIngredient EMPTY = new SizedIngredient(Ingredient.of(), 1);

  /** Creates a sized ingredient from an ingredient with a count of 1 */
  public static SizedIngredient of(Ingredient ingredient) {
    return new SizedIngredient(ingredient, 1);
  }

  /** Creates a sized ingredient from the given ingredient and count */
  public static SizedIngredient of(Ingredient ingredient, int count) {
    return new SizedIngredient(ingredient, count);
  }

  /** Creates a sized ingredient from the given items with a count of 1 */
  public static SizedIngredient fromItems(ItemLike... items) {
    return new SizedIngredient(Ingredient.of(items), 1);
  }

  /** Creates a sized ingredient from the given items and count */
  public static SizedIngredient fromItems(int count, ItemLike... items) {
    return new SizedIngredient(Ingredient.of(items), count);
  }

  /** Creates a sized ingredient from the given tag with a count of 1 */
  public static SizedIngredient fromTag(TagKey<Item> tag) {
    return new SizedIngredient(Ingredient.of(tag), 1);
  }

  /** Creates a sized ingredient from the given tag and count */
  public static SizedIngredient fromTag(TagKey<Item> tag, int count) {
    return new SizedIngredient(Ingredient.of(tag), count);
  }

  /** @return the ingredient, 1.20 style accessor */
  public Ingredient getIngredient() {
    return ingredient;
  }

  /** @return the count, 1.20 style accessor */
  public int getAmountNeeded() {
    return count;
  }

  /** @return stacks matching this ingredient, 1.20 style accessor */
  public ItemStack[] getMatchingStacks() {
    return getItems();
  }

  /** Creates a sized ingredient from the given item */
  public static SizedIngredient of(ItemLike item, int count) {
    return new SizedIngredient(Ingredient.of(item), count);
  }

  /** Creates a sized ingredient from the given tag */
  public static SizedIngredient of(TagKey<Item> tag, int count) {
    return new SizedIngredient(Ingredient.of(tag), count);
  }

  /** Checks if the stack matches the ingredient and has enough items */
  public boolean test(ItemStack stack) {
    return ingredient.test(stack) && stack.getCount() >= count;
  }

  /** Gets all stacks matching this ingredient, with the correct count */
  public ItemStack[] getItems() {
    return Arrays.stream(ingredient.getItems()).map(stack -> stack.copyWithCount(count)).toArray(ItemStack[]::new);
  }
}
