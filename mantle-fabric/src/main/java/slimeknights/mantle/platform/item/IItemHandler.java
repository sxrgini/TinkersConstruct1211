package slimeknights.mantle.platform.item;

import net.minecraft.world.item.ItemStack;

/** Mirror of NeoForge's {@code IItemHandler}. */
public interface IItemHandler {
  int getSlots();

  ItemStack getStackInSlot(int slot);

  ItemStack insertItem(int slot, ItemStack stack, boolean simulate);

  ItemStack extractItem(int slot, int amount, boolean simulate);

  int getSlotLimit(int slot);

  boolean isItemValid(int slot, ItemStack stack);
}
