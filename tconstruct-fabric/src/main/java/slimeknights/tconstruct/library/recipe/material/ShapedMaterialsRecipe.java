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
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import slimeknights.tconstruct.library.materials.definition.MaterialVariantId;
import slimeknights.tconstruct.library.tools.nbt.MaterialNBT;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;
import slimeknights.tconstruct.library.tools.part.IMaterialItem;
import slimeknights.tconstruct.tables.TinkerTables;

import java.util.ArrayList;
import java.util.List;

/**
 * Shaped recipe with a number of {@link slimeknights.tconstruct.library.recipe.ingredient.MaterialIngredient} and
 * {@link slimeknights.tconstruct.library.recipe.ingredient.MaterialValueIngredient} to set the materials of the result.
 */
public class ShapedMaterialsRecipe extends ShapedRecipe implements MaterialsCraftingTableRecipe {
  /** List of tool parts to search for in the final recipe */
  @Getter
  private final List<Ingredient> parts;
  /** Pattern of characters used to look up the parts in the key, null if the recipe came from the network */
  @Nullable
  private final String partPattern;
  /**
   * If true, a part may show up multiple times in the inputs, and all copies should match.
   * If false, only the first instance of a part is checked for each input, allowing a tool with the same part multiple times.
   */
  private final boolean checkRepeats;
  /** List of additional materials to add beyond the parts */
  @Getter
  private final List<MaterialVariantId> extraMaterials;

  private ShapedMaterialsRecipe(ShapedRecipe base, List<Ingredient> parts, @Nullable String partPattern, List<MaterialVariantId> extraMaterials) {
    super(base.getGroup(), base.category(), base.pattern, base.result, base.showNotification());
    this.parts = parts;
    this.partPattern = partPattern;
    this.checkRepeats = parts.stream().unordered().distinct().count() == parts.size();
    this.extraMaterials = extraMaterials;
  }

  /** Creates a recipe from JSON, mapping the part pattern to ingredients using the key */
  private static ShapedMaterialsRecipe fromPattern(ShapedRecipe base, String partPattern, List<MaterialVariantId> extraMaterials) {
    var data = base.pattern.data.orElseThrow(() -> new IllegalArgumentException("Shaped materials recipe must have a key"));
    List<Ingredient> parts = new ArrayList<>(partPattern.length());
    for (int i = 0; i < partPattern.length(); i++) {
      char sym = partPattern.charAt(i);
      Ingredient ingredient = data.key().get(sym);
      if (ingredient == null) {
        throw new IllegalArgumentException("Parts references symbol '" + sym + "' but it's not defined in the key");
      }
      parts.add(ingredient);
    }
    return new ShapedMaterialsRecipe(base, List.copyOf(parts), partPattern, extraMaterials);
  }

  @Override
  public int getPartCount() {
    return parts.size();
  }

  /**
   * Finds materials for each of the parts
   * @return Array of all matched materials. Array will have no null entries, though the array may be null if no match was found.
   */
  @Nullable
  static MaterialVariantId[] findMaterials(CraftingInput inventory, List<Ingredient> parts, int partCount, boolean checkRepeats) {
    // want one material for each
    MaterialVariantId[] materials = new MaterialVariantId[partCount];
    for (int i = 0; i < inventory.size(); i++) {
      ItemStack stack = inventory.getItem(i);
      if (!stack.isEmpty()) {
        for (int p = 0; p < partCount; p++) {
          MaterialVariantId current = materials[p];
          // if we have not found the material yet, or repeats are considered the same material, test the ingredient
          if ((current == null || checkRepeats) && parts.get(p).test(stack)) {
            MaterialVariantId matched = MaterialRecipeCache.getMaterial(stack);
            // first occurrence? thats our material
            if (current == null) {
              materials[p] = matched;
              break;
            } else if (!current.matchesVariant(matched)) {
              // if same material but different variants, just discard the variant
              if (current.getId().equals(matched.getId())) {
                materials[p] = current.getId();
                break;
              } else {
                // if different materials, no match
                return null;
              }
            }
          }
        }
      }
    }
    // ensure we found all materials needed
    for (int p = 0; p < partCount; p++) {
      if (materials[p] == null) {
        return null;
      }
    }
    return materials;
  }

  @Override
  public boolean matches(CraftingInput inventory, Level level) {
    if (!super.matches(inventory, level)) {
      return false;
    }
    // ensure all part materials matched and we found all parts
    return findMaterials(inventory, parts, parts.size(), checkRepeats) != null;
  }

  /** Common logic to this and {@link ShapelessMaterialsRecipe} */
  public static void setMaterial(ItemStack stack, MaterialVariantId material, List<MaterialVariantId> extraMaterials) {
    if (extraMaterials.isEmpty() && stack.getItem() instanceof IMaterialItem materialItem) {
      materialItem.setMaterial(stack, material);
    } else {
      MaterialNBT.Builder builder = MaterialNBT.builder();
      builder.add(material);
      for (MaterialVariantId extraMaterial : extraMaterials) {
        builder.add(extraMaterial);
      }
      ToolStack.from(stack).setMaterials(builder.build());
    }
  }

  /** Sets the material for the given stack */
  @Override
  public void setMaterial(ItemStack stack, MaterialVariantId material) {
    setMaterial(stack, material, extraMaterials);
  }

  /** Assembles the item with material information */
  static ItemStack assemble(ItemStack stack, CraftingInput inventory, List<Ingredient> parts, int partCount, boolean checkRepeats, List<MaterialVariantId> extraMaterials) {
    MaterialVariantId[] materials = findMaterials(inventory, parts, partCount, checkRepeats);
    if (materials != null) {
      // if the result is a tool part, and we only have the one material, set its material
      if (materials.length == 1 && extraMaterials.isEmpty() && stack.getItem() instanceof IMaterialItem materialItem) {
        return materialItem.setMaterial(stack, materials[0]);
      }
      MaterialNBT.Builder builder = MaterialNBT.builder();
      // add each material
      for (MaterialVariantId material : materials) {
        builder.add(material);
      }
      // add extra materials
      builder.add(extraMaterials);
      ToolStack.from(stack).setMaterials(builder.build());
    }
    return stack;
  }

  @Override
  public ItemStack assemble(CraftingInput inventory, HolderLookup.Provider registryAccess) {
    return assemble(super.assemble(inventory, registryAccess), inventory, parts, parts.size(), checkRepeats, extraMaterials);
  }

  @Override
  public RecipeSerializer<?> getSerializer() {
    return TinkerTables.shapedMaterialsRecipeSerializer.get();
  }

  public static class Serializer implements RecipeSerializer<ShapedMaterialsRecipe> {
    static final Codec<List<MaterialVariantId>> EXTRA_MATERIALS = MaterialVariantId.LOADABLE.list(0).asCodec();
    static final StreamCodec<RegistryFriendlyByteBuf,List<MaterialVariantId>> EXTRA_MATERIALS_STREAM = MaterialVariantId.LOADABLE.list(0).asStreamCodec();
    private static final StreamCodec<RegistryFriendlyByteBuf,List<Ingredient>> PARTS_STREAM = Ingredient.CONTENTS_STREAM_CODEC.apply(ByteBufCodecs.list());

    private static final MapCodec<ShapedMaterialsRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
      SHAPED_RECIPE.codec().forGetter(r -> r),
      Codec.STRING.fieldOf("parts").forGetter(r -> r.partPattern == null ? "" : r.partPattern),
      EXTRA_MATERIALS.optionalFieldOf("extra_materials", List.of()).forGetter(r -> r.extraMaterials)
    ).apply(instance, ShapedMaterialsRecipe::fromPattern));

    private static final StreamCodec<RegistryFriendlyByteBuf,ShapedMaterialsRecipe> STREAM_CODEC = StreamCodec.of(
      (buffer, recipe) -> {
        SHAPED_RECIPE.streamCodec().encode(buffer, recipe);
        PARTS_STREAM.encode(buffer, recipe.parts);
        EXTRA_MATERIALS_STREAM.encode(buffer, recipe.extraMaterials);
      },
      buffer -> {
        ShapedRecipe base = SHAPED_RECIPE.streamCodec().decode(buffer);
        List<Ingredient> parts = PARTS_STREAM.decode(buffer);
        return new ShapedMaterialsRecipe(base, List.copyOf(parts), null, EXTRA_MATERIALS_STREAM.decode(buffer));
      });

    @Override
    public MapCodec<ShapedMaterialsRecipe> codec() {
      return CODEC;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf,ShapedMaterialsRecipe> streamCodec() {
      return STREAM_CODEC;
    }
  }
}
