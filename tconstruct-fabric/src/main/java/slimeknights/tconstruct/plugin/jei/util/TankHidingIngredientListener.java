package slimeknights.tconstruct.plugin.jei.util;

import slimeknights.mantle.platform.capability.Caps;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.forge.ForgeTypes;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.api.runtime.IIngredientManager.IIngredientListener;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import slimeknights.mantle.platform.capability.Capabilities;
import slimeknights.mantle.platform.fluid.FluidStack;
import slimeknights.mantle.platform.fluid.IFluidHandler.FluidAction;
import slimeknights.mantle.platform.fluid.IFluidHandlerItem;
import slimeknights.tconstruct.library.fluid.EmptyFluidHandlerItem;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/** Handler to remove tanks when their fluid is removed from JEI. */
public record TankHidingIngredientListener(IIngredientManager manager, List<Item> tanks) implements IIngredientListener {
  /** Gets all changed tanks from the given ingredients */
  private List<ItemStack> getTanks(Collection<? extends ITypedIngredient<?>> ingredients) {
    List<ItemStack> list = new ArrayList<>();
    for (ITypedIngredient<?> ingredient : ingredients) {
      FluidStack fluid = ingredient.getIngredient(ForgeTypes.FLUID_STACK).orElse(FluidStack.EMPTY);
      if (!fluid.isEmpty()) {
        for (Item item : tanks) {
          ItemStack tank = new ItemStack(item);
          IFluidHandlerItem handler = Caps.get(tank, Capabilities.FLUID_HANDLER_ITEM).orElse(EmptyFluidHandlerItem.INSTANCE);
          if (handler.getTanks() > 0 && handler.fill(fluid, FluidAction.EXECUTE) > 0) {
            list.add(handler.getContainer());
          }
        }
      }
    }
    return list;
  }

  @Override
  public <V> void onIngredientsAdded(IIngredientHelper<V> ingredientHelper, Collection<ITypedIngredient<V>> ingredients) {
    List<ItemStack> tanks = getTanks(ingredients);
    if (!tanks.isEmpty()) {
      manager.addIngredientsAtRuntime(VanillaTypes.ITEM_STACK, tanks);
    }
  }

  @Override
  public <V> void onIngredientsRemoved(IIngredientHelper<V> ingredientHelper, Collection<ITypedIngredient<V>> ingredients) {
    List<ItemStack> tanks = getTanks(ingredients);
    if (!tanks.isEmpty()) {
      manager.removeIngredientsAtRuntime(VanillaTypes.ITEM_STACK, tanks);
    }
  }
}
