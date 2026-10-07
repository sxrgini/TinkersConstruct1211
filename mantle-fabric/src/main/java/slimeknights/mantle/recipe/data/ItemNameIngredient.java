package slimeknights.mantle.recipe.data;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import slimeknights.mantle.platform.ingredient.ICustomIngredient;
import slimeknights.mantle.platform.ingredient.IngredientType;
import slimeknights.mantle.recipe.MantleRecipes;

import java.util.stream.Stream;

/** Ingredient matching an item by registry name, which does not fail to load if the item is missing. Mainly used for compat with other mods in recipes. */
public record ItemNameIngredient(ResourceLocation name) implements ICustomIngredient {
  public static final MapCodec<ItemNameIngredient> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
    ResourceLocation.CODEC.fieldOf("name").forGetter(ItemNameIngredient::name)
  ).apply(instance, ItemNameIngredient::new));
  public static final StreamCodec<RegistryFriendlyByteBuf,ItemNameIngredient> STREAM_CODEC = ResourceLocation.STREAM_CODEC.map(ItemNameIngredient::new, ItemNameIngredient::name).mapStream(buf -> buf);

  /** Creates an ingredient matching the item with the given name */
  public static Ingredient from(ResourceLocation name) {
    return new ItemNameIngredient(name).toVanilla();
  }

  /** Creates an ingredient matching any of the items with the given names */
  public static Ingredient from(ResourceLocation... names) {
    Ingredient[] ingredients = new Ingredient[names.length];
    for (int i = 0; i < names.length; i++) {
      ingredients[i] = from(names[i]);
    }
    return net.fabricmc.fabric.api.recipe.v1.ingredient.DefaultCustomIngredients.any(ingredients);
  }

  @Override
  public boolean test(ItemStack stack) {
    return BuiltInRegistries.ITEM.getKey(stack.getItem()).equals(name);
  }

  @Override
  public Stream<ItemStack> getItems() {
    return BuiltInRegistries.ITEM.getOptional(name).map(item -> new ItemStack(item)).stream();
  }

  @Override
  public boolean isSimple() {
    return true;
  }

  @Override
  public IngredientType<ItemNameIngredient> getType() {
    return MantleRecipes.ITEM_NAME_INGREDIENT;
  }
}
