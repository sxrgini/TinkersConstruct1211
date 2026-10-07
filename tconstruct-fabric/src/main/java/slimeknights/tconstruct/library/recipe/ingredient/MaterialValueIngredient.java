package slimeknights.tconstruct.library.recipe.ingredient;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.Nullable;
import slimeknights.mantle.data.predicate.IJsonPredicate;
import slimeknights.mantle.platform.ingredient.ICustomIngredient;
import slimeknights.mantle.platform.ingredient.IngredientType;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.library.json.predicate.material.MaterialPredicate;
import slimeknights.tconstruct.library.materials.definition.MaterialVariantId;
import slimeknights.tconstruct.library.recipe.material.MaterialRecipe;
import slimeknights.tconstruct.library.recipe.material.MaterialRecipeCache;

import java.util.Arrays;
import java.util.stream.Stream;

/**
 * Ingredient matching material items with the given value. Typically, matches ingots or blocks
 */
public class MaterialValueIngredient implements ICustomIngredient {
  public static final ResourceLocation ID = TConstruct.getResource("material_value");
  public static final MapCodec<MaterialValueIngredient> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
    MaterialPredicate.CODEC.optionalFieldOf("material", MaterialPredicate.ANY).forGetter(MaterialValueIngredient::getMaterial),
    Codec.FLOAT.optionalFieldOf("min", 0f).forGetter(MaterialValueIngredient::getMinValue),
    Codec.FLOAT.optionalFieldOf("max", Float.POSITIVE_INFINITY).forGetter(MaterialValueIngredient::getMaxValue)
  ).apply(instance, MaterialValueIngredient::new));
  public static final StreamCodec<RegistryFriendlyByteBuf,MaterialValueIngredient> STREAM_CODEC = StreamCodec.composite(
    MaterialPredicate.STREAM_CODEC, MaterialValueIngredient::getMaterial,
    ByteBufCodecs.FLOAT, MaterialValueIngredient::getMinValue,
    ByteBufCodecs.FLOAT, MaterialValueIngredient::getMaxValue,
    MaterialValueIngredient::new);
  public static final IngredientType<MaterialValueIngredient> TYPE = new IngredientType<>(CODEC, STREAM_CODEC);

  private final IJsonPredicate<MaterialVariantId> material;
  private final float minValue;
  private final float maxValue;

  public MaterialValueIngredient(IJsonPredicate<MaterialVariantId> material, float minValue, float maxValue) {
    this.material = material;
    this.minValue = minValue;
    this.maxValue = maxValue;
  }

  public IJsonPredicate<MaterialVariantId> getMaterial() {
    return material;
  }

  public float getMinValue() {
    return minValue;
  }

  public float getMaxValue() {
    return maxValue;
  }

  /** Creates an ingredient matching a range of values */
  public static Ingredient of(IJsonPredicate<MaterialVariantId> materials, float minValue, float maxValue) {
    return new MaterialValueIngredient(materials, minValue, maxValue).toVanilla();
  }

  /** Creates an ingredient matching an exact value */
  public static Ingredient of(IJsonPredicate<MaterialVariantId> materials, float value) {
    return of(materials, value, value);
  }

  /** Checks the given material recipe against our filters */
  public boolean test(MaterialRecipe material) {
    float value = material.getValue() / (float) material.getNeeded();
    return minValue <= value && value <= maxValue && this.material.matches(material.getMaterial().getVariant());
  }

  @Override
  public boolean test(ItemStack stack) {
    MaterialRecipe recipe = MaterialRecipeCache.findRecipe(stack);
    return recipe != MaterialRecipe.EMPTY && test(recipe);
  }

  @Override
  public Stream<ItemStack> getItems() {
    return MaterialRecipeCache.getSortedRecipes().stream()
      .filter(this::test)
      .flatMap(material -> Arrays.stream(material.getIngredient().getItems()));
  }

  @Override
  public boolean isSimple() {
    return true;
  }

  @Override
  public IngredientType<?> getType() {
    return TYPE;
  }


  /* Helpers for ShapedMaterialRecipe */

  /** Checks if this ingredient fully contains the range of the other */
  private boolean contains(MaterialValueIngredient other) {
    return this.minValue <= other.minValue && other.maxValue <= this.maxValue;
  }

  /** Creates an ingredient that matches anything either of the two ingredients matches */
  public MaterialValueIngredient merge(MaterialValueIngredient other) {
    if (this == other) return this;

    // if we have the same predicate, we can possibly skip creating a new instance
    IJsonPredicate<MaterialVariantId> predicate = this.material;
    if (this.material.equals(other.material)) {
      if (this.contains(other)) {
        return this;
      }
      if (other.contains(this)) {
        return other;
      }
    } else {
      predicate = MaterialPredicate.or(this.material, other.material);
    }
    return new MaterialValueIngredient(predicate, Math.min(this.minValue, other.minValue), Math.max(this.maxValue, other.maxValue));
  }

  /** Gets the material matching this recipe */
  @Nullable
  public MaterialVariantId getMaterial(ItemStack stack) {
    MaterialRecipe recipe = MaterialRecipeCache.findRecipe(stack);
    return recipe != MaterialRecipe.EMPTY && test(recipe) ? recipe.getMaterial().getVariant() : null;
  }
}
