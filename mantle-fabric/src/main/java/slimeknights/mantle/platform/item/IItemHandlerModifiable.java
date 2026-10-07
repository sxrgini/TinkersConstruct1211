package slimeknights.mantle.platform.item;

import net.minecraft.world.item.ItemStack;

/** Item handler that allows directly setting slots. */
public interface IItemHandlerModifiable extends IItemHandler {
  void setStackInSlot(int slot, ItemStack stack);
}
