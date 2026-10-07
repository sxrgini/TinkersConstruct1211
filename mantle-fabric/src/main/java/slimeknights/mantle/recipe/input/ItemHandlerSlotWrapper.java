package slimeknights.mantle.recipe.input;

import lombok.AllArgsConstructor;
import net.minecraft.world.item.ItemStack;
import slimeknights.mantle.platform.item.IItemHandler;

/**
 * Implementation of {@link SingleItemInput} to wrap a {@link IItemHandler}
 */
@AllArgsConstructor
public class ItemHandlerSlotWrapper implements SingleItemInput {
  private final IItemHandler parent;
  private final int index;

  @Override
  public ItemStack getItem() {
    return parent.getStackInSlot(index);
  }
}
