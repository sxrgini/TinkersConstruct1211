package slimeknights.mantle.platform.item;

import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;

/** Wraps a vanilla {@link Container} as an item handler. */
public class InvWrapper implements IItemHandlerModifiable {
  private final Container inv;

  public InvWrapper(Container inv) {
    this.inv = inv;
  }

  public Container getInv() {
    return inv;
  }

  @Override
  public int getSlots() {
    return inv.getContainerSize();
  }

  @Override
  public ItemStack getStackInSlot(int slot) {
    return inv.getItem(slot);
  }

  @Override
  public void setStackInSlot(int slot, ItemStack stack) {
    inv.setItem(slot, stack);
  }

  @Override
  public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
    if (stack.isEmpty() || !isItemValid(slot, stack)) {
      return stack;
    }
    ItemStack existing = inv.getItem(slot);
    int limit = Math.min(getSlotLimit(slot), stack.getMaxStackSize());
    if (!existing.isEmpty()) {
      if (!ItemStack.isSameItemSameComponents(stack, existing)) {
        return stack;
      }
      limit -= existing.getCount();
    }
    if (limit <= 0) {
      return stack;
    }
    boolean reachedLimit = stack.getCount() > limit;
    if (!simulate) {
      if (existing.isEmpty()) {
        inv.setItem(slot, reachedLimit ? stack.copyWithCount(limit) : stack.copy());
      } else {
        existing.grow(reachedLimit ? limit : stack.getCount());
      }
      inv.setChanged();
    }
    return reachedLimit ? stack.copyWithCount(stack.getCount() - limit) : ItemStack.EMPTY;
  }

  @Override
  public ItemStack extractItem(int slot, int amount, boolean simulate) {
    if (amount == 0) {
      return ItemStack.EMPTY;
    }
    ItemStack existing = inv.getItem(slot);
    if (existing.isEmpty()) {
      return ItemStack.EMPTY;
    }
    int toExtract = Math.min(amount, existing.getMaxStackSize());
    if (existing.getCount() <= toExtract) {
      if (!simulate) {
        inv.setItem(slot, ItemStack.EMPTY);
        inv.setChanged();
      }
      return existing;
    }
    if (!simulate) {
      inv.setItem(slot, existing.copyWithCount(existing.getCount() - toExtract));
      inv.setChanged();
    }
    return existing.copyWithCount(toExtract);
  }

  @Override
  public int getSlotLimit(int slot) {
    return inv.getMaxStackSize();
  }

  @Override
  public boolean isItemValid(int slot, ItemStack stack) {
    return inv.canPlaceItem(slot, stack);
  }
}
