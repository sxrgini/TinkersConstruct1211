package slimeknights.tconstruct.library.recipe.material;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import slimeknights.tconstruct.library.materials.definition.MaterialVariantId;
import slimeknights.tconstruct.tables.TinkerTables;

import java.util.List;

/**
 * Shapeless recipe with a number of {@link slimeknights.tconstruct.library.recipe.ingredient.MaterialIngredient} and
 * {@link slimeknights.tconstruct.library.recipe.ingredient.MaterialValueIngredient} to set the materials of the result.
 */
public class ShapelessMaterialsRecipe extends ShapelessRecipe implements MaterialsCraftingTableRecipe {
  /** Number of parts to match */
  @Getter
  private final int partCount;
  /** List of additional materials to add beyond the parts */
  @Getter
  private final List<MaterialVariantId> extraMaterials;

  public ShapelessMaterialsRecipe(ShapelessRecipe recipe, int partCount, List<MaterialVariantId> extraMaterials) {
    super(recipe.getGroup(), recipe.category(), recipe.result, recipe.getIngredients());
    this.partCount = partCount;
    this.extraMaterials = extraMaterials;
  }

  @Override
  public List<Ingredient> getParts() {
    return getIngredients();
  }

  /** Sets the material for the given stack */
  @Override
  public void setMaterial(ItemStack stack, MaterialVariantId material) {
    ShapedMaterialsRecipe.setMaterial(stack, material, extraMaterials);
  }

  @Override
  public ItemStack assemble(CraftingInput inventory, HolderLookup.Provider registryAccess) {
    return ShapedMaterialsRecipe.assemble(super.assemble(inventory, registryAccess), inventory, getIngredients(), partCount, false, extraMaterials);
  }

  @Override
  public RecipeSerializer<?> getSerializer() {
    return TinkerTables.shapelessMaterialsRecipeSerializer.get();
  }

  public static class Serializer implements RecipeSerializer<ShapelessMaterialsRecipe> {
    private static final MapCodec<ShapelessMaterialsRecipe> CODEC = RecordCodecBuilder.<ShapelessMaterialsRecipe>mapCodec(instance -> instance.group(
      SHAPELESS_RECIPE.codec().forGetter(r -> r),
      Codec.intRange(1, 9).fieldOf("parts").forGetter(r -> r.partCount),
      ShapedMaterialsRecipe.Serializer.EXTRA_MATERIALS.optionalFieldOf("extra_materials", List.of()).forGetter(r -> r.extraMaterials)
    ).apply(instance, ShapelessMaterialsRecipe::new)).validate(recipe -> {
      if (recipe.partCount > recipe.getIngredients().size()) {
        return com.mojang.serialization.DataResult.error(() -> "Parts must be between 1 and the number of ingredients " + recipe.getIngredients().size());
      }
      return com.mojang.serialization.DataResult.success(recipe);
    });

    private static final StreamCodec<RegistryFriendlyByteBuf,ShapelessMaterialsRecipe> STREAM_CODEC = StreamCodec.of(
      (buffer, recipe) -> {
        SHAPELESS_RECIPE.streamCodec().encode(buffer, recipe);
        buffer.writeByte(recipe.partCount);
        ShapedMaterialsRecipe.Serializer.EXTRA_MATERIALS_STREAM.encode(buffer, recipe.extraMaterials);
      },
      buffer -> new ShapelessMaterialsRecipe(SHAPELESS_RECIPE.streamCodec().decode(buffer), buffer.readByte(), ShapedMaterialsRecipe.Serializer.EXTRA_MATERIALS_STREAM.decode(buffer)));

    @Override
    public MapCodec<ShapelessMaterialsRecipe> codec() {
      return CODEC;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf,ShapelessMaterialsRecipe> streamCodec() {
      return STREAM_CODEC;
    }
  }
}
