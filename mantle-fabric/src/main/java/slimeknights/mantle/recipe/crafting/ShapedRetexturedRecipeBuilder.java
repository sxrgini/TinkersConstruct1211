package slimeknights.mantle.recipe.crafting;

import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import slimeknights.mantle.recipe.crafting.ShapedRetexturedRecipe.Pattern;

/** Builder for {@link ShapedRetexturedRecipe} */
@SuppressWarnings("unused")
public class ShapedRetexturedRecipeBuilder extends ShapedExtensionBuilder<ShapedRetexturedRecipeBuilder> {
  private char textureKey = '\0';
  private boolean matchAll = false;

  protected ShapedRetexturedRecipeBuilder(ItemStack result) {
    super(result);
  }

  /** Creates a builder for the given stack */
  public static ShapedRetexturedRecipeBuilder shaped(ItemStack result) {
    return new ShapedRetexturedRecipeBuilder(result);
  }

  /** Creates a builder for the given item and count */
  public static ShapedRetexturedRecipeBuilder shaped(ItemLike result, int count) {
    return shaped(new ItemStack(result, count));
  }

  /** Creates a builder for the given item */
  public static ShapedRetexturedRecipeBuilder shaped(ItemLike result) {
    return shaped(result, 1);
  }

  /** Creates a builder copying the properties of a vanilla shaped recipe builder */
  public static ShapedRetexturedRecipeBuilder fromShaped(net.minecraft.data.recipes.ShapedRecipeBuilder base) {
    ShapedRetexturedRecipeBuilder builder = shaped(base.result, base.count);
    builder.category(base.category);
    base.rows.forEach(builder::pattern);
    base.key.forEach(builder::define);
    base.criteria.forEach(builder::unlockedBy);
    if (base.group != null) {
      builder.group(base.group);
    }
    return builder;
  }

  /** Sets the texture source to a key from the texture map, alias of {@link #source(char)} */
  public ShapedRetexturedRecipeBuilder setSource(char textureKey) {
    return source(textureKey);
  }

  /** Saves the recipe, alias of {@link #save(RecipeOutput, ResourceLocation)} */
  public void build(RecipeOutput output, ResourceLocation id) {
    save(output, id);
  }

  /** Sets the texture source to a key from the texture map. */
  public ShapedRetexturedRecipeBuilder source(char textureKey) {
    this.textureKey = textureKey;
    return this;
  }

  /**
   * Sets the match first property on the recipe.
   * If set, the recipe uses the first ingredient match for the texture. If unset, all items that match the ingredient must be the same or no texture is applied
   * @return Builder instance
   */
  public ShapedRetexturedRecipeBuilder setMatchAll() {
    this.matchAll = true;
    return this;
  }

  @Override
  public void save(RecipeOutput output, ResourceLocation id) {
    if (textureKey == '\0') {
      throw new IllegalStateException("No texture defined for texture recipe");
    }
    if (!this.key.containsKey(textureKey)) {
      throw new IllegalStateException("Texture references symbol '" + textureKey + "' but it's not defined in the key");
    }
    output.accept(id,
      new ShapedRetexturedRecipe(group, getBookCategory(), new Pattern(getPattern(), Ingredient.EMPTY, textureKey), result, showNotification, matchAll),
      buildAdvancement(output, id, category.getFolderName())
    );
  }
}
