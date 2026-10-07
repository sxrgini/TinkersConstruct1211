package slimeknights.tconstruct.library.recipe.ingredient;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import slimeknights.mantle.data.predicate.IJsonPredicate;
import slimeknights.mantle.platform.ingredient.IngredientType;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.library.json.predicate.material.MaterialPredicate;
import slimeknights.tconstruct.library.materials.MaterialRegistry;
import slimeknights.tconstruct.library.materials.definition.IMaterial;
import slimeknights.tconstruct.library.materials.definition.MaterialVariantId;
import slimeknights.tconstruct.library.recipe.material.MaterialRecipeCache;
import slimeknights.tconstruct.library.tools.part.IMaterialItem;

import java.util.Arrays;
import java.util.stream.Stream;

/**
 * Extension of the vanilla ingredient to display materials on items and support matching by materials
 */
public class MaterialIngredient extends NestedIngredient {
  public static final ResourceLocation ID = TConstruct.getResource("material");
  public static final MapCodec<MaterialIngredient> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
    Ingredient.CODEC.fieldOf("match").forGetter(NestedIngredient::getNested),
    MaterialPredicate.CODEC.optionalFieldOf("material", MaterialPredicate.ANY).forGetter(i -> i.material)
  ).apply(instance, MaterialIngredient::new));
  public static final StreamCodec<RegistryFriendlyByteBuf,MaterialIngredient> STREAM_CODEC = StreamCodec.composite(
    Ingredient.CONTENTS_STREAM_CODEC, NestedIngredient::getNested,
    MaterialPredicate.STREAM_CODEC, i -> i.material,
    MaterialIngredient::new);
  public static final IngredientType<MaterialIngredient> TYPE = new IngredientType<>(CODEC, STREAM_CODEC);

  private final IJsonPredicate<MaterialVariantId> material;
  protected MaterialIngredient(Ingredient nested, IJsonPredicate<MaterialVariantId> material) {
    super(nested);
    this.material = material;
  }

  /** Gets the material predicate */
  public IJsonPredicate<MaterialVariantId> getMaterialPredicate() {
    return material;
  }

  /** Creates an ingredient matching the given materials */
  public static Ingredient of(Ingredient ingredient, IJsonPredicate<MaterialVariantId> material) {
    return new MaterialIngredient(ingredient, material).toVanilla();
  }

  /** Creates an ingredient matching the given materials */
  public static Ingredient of(ItemLike item, IJsonPredicate<MaterialVariantId> material) {
    return of(Ingredient.of(item), material);
  }

  /** Creates an ingredient matching any material */
  public static Ingredient of(Ingredient ingredient) {
    return of(ingredient, MaterialPredicate.ANY);
  }

  /** Creates an ingredient matching a single material */
  public static Ingredient of(Ingredient ingredient, MaterialVariantId material) {
    return of(ingredient, MaterialPredicate.variant(material));
  }

  /** Creates an ingredient matching a material tag */
  public static Ingredient of(Ingredient ingredient, TagKey<IMaterial> tag) {
    return of(ingredient, MaterialPredicate.tag(tag));
  }

  /** Creates a new instance from an item with a fixed material */
  public static Ingredient of(ItemLike item, MaterialVariantId material) {
    return of(Ingredient.of(item), material);
  }

  /** Creates a new instance from an item with a tagged material */
  public static Ingredient of(ItemLike item, TagKey<IMaterial> tag) {
    return of(Ingredient.of(item), tag);
  }

  /** Creates a new ingredient matching any material from items */
  public static Ingredient of(ItemLike item) {
    return of(Ingredient.of(item));
  }

  /** Creates a new ingredient from a tag with a material */
  public static Ingredient of(TagKey<Item> tag, MaterialVariantId material) {
    return of(Ingredient.of(tag), material);
  }

  /** Creates a new ingredient matching any material from a tag */
  public static Ingredient of(TagKey<Item> tag) {
    return of(Ingredient.of(tag));
  }

  @Override
  public boolean test(ItemStack stack) {
    // check super first, should be faster
    if (stack.isEmpty() || !super.test(stack)) {
      return false;
    }
    // no need to read material data if the material is the any predicate
    if (material != MaterialPredicate.ANY) {
      return material.matches(IMaterialItem.getMaterialFromStack(stack));
    }
    return true;
  }

  @Override
  public Stream<ItemStack> getItems() {
    if (!MaterialRegistry.isFullyLoaded()) {
      return super.getItems();
    }
    // no material? apply all materials for variants, note this only shows craftable material variants
    return Arrays.stream(nested.getItems())
      .flatMap(stack -> MaterialRecipeCache.getAllVariants().stream()
        .filter(material::matches)
        .map(mat -> IMaterialItem.withMaterial(stack, mat)))
      .distinct();
  }

  @Override
  public boolean isSimple() {
    return material == MaterialPredicate.ANY;
  }

  @Override
  public IngredientType<?> getType() {
    return TYPE;
  }
}
