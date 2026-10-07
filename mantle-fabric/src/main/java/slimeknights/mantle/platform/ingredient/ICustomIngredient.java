package slimeknights.mantle.platform.ingredient;

import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredient;
import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredientSerializer;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.stream.Stream;

/** Adapter mirroring NeoForge's {@code ICustomIngredient} on top of Fabric's {@link CustomIngredient}. */
public interface ICustomIngredient extends CustomIngredient {
  /** Gets the type of this ingredient, used for serialization */
  IngredientType<?> getType();

  /** Gets all matching items */
  Stream<ItemStack> getItems();

  /** If true, matching only depends on the item, not any stack data */
  boolean isSimple();

  @Override
  default List<ItemStack> getMatchingStacks() {
    return getItems().toList();
  }

  @Override
  default boolean requiresTesting() {
    return !isSimple();
  }

  @Override
  default CustomIngredientSerializer<?> getSerializer() {
    return getType();
  }
}
