package slimeknights.tconstruct.library.recipe.casting;

import slimeknights.tconstruct.library.utils.PotionHelper;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import slimeknights.mantle.platform.fluid.FluidStack;
import slimeknights.mantle.data.loadable.field.ContextKey;
import slimeknights.mantle.data.loadable.field.LoadableField;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.mantle.recipe.helper.LoadableRecipeSerializer;
import slimeknights.mantle.recipe.helper.TypeAwareRecipeSerializer;
import slimeknights.mantle.recipe.ingredient.FluidIngredient;
import slimeknights.tconstruct.library.modifiers.ModifierId;
import slimeknights.tconstruct.library.recipe.casting.potion.ModifierPotionCastingRecipe;
import slimeknights.tconstruct.library.tools.helper.ModifierUtil;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Casting recipe applying a potion to a tool.
 * TODO 1.21: move to {@link slimeknights.tconstruct.library.recipe.casting.potion}
 */
public class TippingCastingRecipe extends ModifierPotionCastingRecipe {
  protected static final LoadableField<Ingredient, ModifierPotionCastingRecipe> TOOL_FIELD = ModifierPotionCastingRecipe.TOOL_FIELD;
  public static final RecordLoadable<TippingCastingRecipe> LOADER = RecordLoadable.create(
    LoadableRecipeSerializer.TYPED_SERIALIZER.requiredField(), LoadableRecipeSerializer.RECIPE_GROUP,
    TOOL_FIELD, FLUID_FIELD, COOLING_TIME_FIELD, MODIFIER_FIELD, TippingCastingRecipe::new);

  public TippingCastingRecipe(TypeAwareRecipeSerializer<?> serializer, String group, Ingredient tool, FluidIngredient fluid, int coolingTime, ModifierId modifier) {
    super(serializer, group, tool, fluid, Items.AIR, coolingTime, modifier);
  }

  @Override
  public boolean matches(ICastingContainer inv, Level level) {
    // must have the modifier to cast
    if (super.matches(inv, level)) {
      // must also have a specific potion, it's what we are going to copy
      // but it can't match what is already on the stack
      String potionId = PotionHelper.getPotionId(inv.getFluidStack());
      return !potionId.isEmpty() && !ModifierUtil.getPersistentString(inv.getStack(), modifier.location()).equals(potionId);
    }
    return false;
  }

  @Override
  public ItemStack assemble(ICastingContainer inv, HolderLookup.Provider access) {
    ItemStack result = inv.getStack().copy();
    String potionId = PotionHelper.getPotionId(inv.getFluidStack());
    if (!potionId.isEmpty()) {
      ToolStack.from(result).getPersistentData().putString(modifier, potionId);
    }
    return result;
  }


  /* JEI */

  @Override
  public List<IDisplayableCastingRecipe> getRecipes(HolderLookup.Provider access) {
    if (displayRecipes == null) {
      // first, get a list of potion IDs
      List<String> potions = getPotionIds();
      List<net.minecraft.core.component.DataComponentPatch> potionNBT = potions.stream().map(id -> {
        var potion = PotionHelper.byId(id);
        return net.minecraft.core.component.DataComponentPatch.builder().set(net.minecraft.core.component.DataComponents.POTION_CONTENTS, new net.minecraft.world.item.alchemy.PotionContents(potion)).build();
      }).toList();

      // next, make 1 copy of the potion fluid with NBT list per potion fluid
      List<FluidStack> basePotions = this.fluid.getFluids();
      List<FluidStack> fluids = basePotions.stream()
        .flatMap(fluid -> potionNBT.stream().map(nbt -> new FluidStack(fluid.getFluid(), fluid.getAmount(), nbt)))
        .toList();

      // finally, create 1 recipe per input tool
      int basePotionCount = basePotions.size();
      displayRecipes = Arrays.stream(bottle.getItems()).map(stack -> {
        // start with just the tool with the modifier
        ItemStack withModifier = addModifier(stack);
        // next, add the potion to the tools
        List<ItemStack> withPotion = addPotion(withModifier, potions);
        // duplicate the list if we have multiple potion fluids
        if (basePotionCount > 1) {
          List<ItemStack> list = new ArrayList<>(withPotion.size() * basePotionCount);
          for (int i = 0; i < basePotionCount; i++) {
            list.addAll(withPotion);
          }
          withPotion = List.copyOf(list);
        }
        // and finally create the recipe
        return DisplayCastingRecipe.from(this)
          .cast(withModifier).consumed()
          .results(withPotion).fluids(fluids).linkFluidsToOutput()
          .coolingTime(coolingTime).build();
      }).toList();
    }
    return displayRecipes;
  }
}
