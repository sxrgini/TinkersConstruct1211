package slimeknights.mantle.platform.item;

import net.minecraft.core.Direction;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/** Wraps a sided container as an item handler, only exposing the slots available from the given side */
public class SidedInvWrapper implements IItemHandlerModifiable {
  private final WorldlyContainer inv;
  @Nullable
  private final Direction side;
  private final int[] slots;

  public SidedInvWrapper(WorldlyContainer inv, @Nullable Direction side) {
    this.inv = inv;
    this.side = side;
    this.slots = side == null ? new int[0] : inv.getSlotsForFace(side);
  }

  private int slot(int slot) {
    return slot >= 0 && slot < slots.length ? slots[slot] : -1;
  }

  @Override
  public int getSlots() {
    return slots.length;
  }

  @Override
  public ItemStack getStackInSlot(int slot) {
    int real = slot(slot);
    return real == -1 ? ItemStack.EMPTY : inv.getItem(real);
  }

  @Override
  public void setStackInSlot(int slot, ItemStack stack) {
    int real = slot(slot);
    if (real != -1) {
      inv.setItem(real, stack);
    }
  }

  @Override
  public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
    int real = slot(slot);
    if (stack.isEmpty() || real == -1 || !inv.canPlaceItem(real, stack) || (side != null && !inv.canPlaceItemThroughFace(real, stack, side))) {
      return stack;
    }
    ItemStack existing = inv.getItem(real);
    int limit = Math.min(inv.getMaxStackSize(), stack.getMaxStackSize());
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
        inv.setItem(real, reachedLimit ? stack.copyWithCount(limit) : stack.copy());
      } else {
        existing.grow(reachedLimit ? limit : stack.getCount());
      }
      inv.setChanged();
    }
    return reachedLimit ? stack.copyWithCount(stack.getCount() - limit) : ItemStack.EMPTY;
  }

  @Override
  public ItemStack extractItem(int slot, int amount, boolean simulate) {
    int real = slot(slot);
    if (amount == 0 || real == -1) {
      return ItemStack.EMPTY;
    }
    ItemStack existing = inv.getItem(real);
    if (existing.isEmpty() || (side != null && !inv.canTakeItemThroughFace(real, existing, side))) {
      return ItemStack.EMPTY;
    }
    int toExtract = Math.min(amount, existing.getCount());
    if (simulate) {
      return existing.copyWithCount(toExtract);
    }
    ItemStack result = inv.removeItem(real, toExtract);
    inv.setChanged();
    return result;
  }

  @Override
  public int getSlotLimit(int slot) {
    return inv.getMaxStackSize();
  }

  @Override
  public boolean isItemValid(int slot, ItemStack stack) {
    int real = slot(slot);
    return real != -1 && inv.canPlaceItem(real, stack);
  }
}
