package slimeknights.mantle.platform.item;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.item.ItemStack;

/** Simple item handler backed by a list, replacing Forge's {@code ItemStackHandler} */
public class ItemStackHandler implements IItemHandlerModifiable {
  protected NonNullList<ItemStack> stacks;

  public ItemStackHandler() {
    this(1);
  }

  public ItemStackHandler(int size) {
    this.stacks = NonNullList.withSize(size, ItemStack.EMPTY);
  }

  public void setSize(int size) {
    this.stacks = NonNullList.withSize(size, ItemStack.EMPTY);
  }

  @Override
  public void setStackInSlot(int slot, ItemStack stack) {
    stacks.set(slot, stack);
    onContentsChanged(slot);
  }

  @Override
  public int getSlots() {
    return stacks.size();
  }

  @Override
  public ItemStack getStackInSlot(int slot) {
    return stacks.get(slot);
  }

  @Override
  public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
    if (stack.isEmpty() || !isItemValid(slot, stack)) {
      return stack;
    }
    ItemStack existing = stacks.get(slot);
    int limit = Math.min(getSlotLimit(slot), stack.getMaxStackSize());
    if (!existing.isEmpty()) {
      if (!ItemHandlerHelper.canItemStacksStack(stack, existing)) {
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
        stacks.set(slot, reachedLimit ? stack.copyWithCount(limit) : stack);
      } else {
        existing.grow(reachedLimit ? limit : stack.getCount());
      }
      onContentsChanged(slot);
    }
    return reachedLimit ? stack.copyWithCount(stack.getCount() - limit) : ItemStack.EMPTY;
  }

  @Override
  public ItemStack extractItem(int slot, int amount, boolean simulate) {
    if (amount == 0) {
      return ItemStack.EMPTY;
    }
    ItemStack existing = stacks.get(slot);
    if (existing.isEmpty()) {
      return ItemStack.EMPTY;
    }
    int toExtract = Math.min(amount, existing.getMaxStackSize());
    if (existing.getCount() <= toExtract) {
      if (!simulate) {
        stacks.set(slot, ItemStack.EMPTY);
        onContentsChanged(slot);
        return existing;
      }
      return existing.copy();
    }
    if (!simulate) {
      stacks.set(slot, existing.copyWithCount(existing.getCount() - toExtract));
      onContentsChanged(slot);
    }
    return existing.copyWithCount(toExtract);
  }

  @Override
  public int getSlotLimit(int slot) {
    return 64;
  }

  @Override
  public boolean isItemValid(int slot, ItemStack stack) {
    return true;
  }

  public CompoundTag serializeNBT(HolderLookup.Provider registries) {
    CompoundTag tag = new CompoundTag();
    ContainerHelper.saveAllItems(tag, stacks, registries);
    return tag;
  }

  public void deserializeNBT(HolderLookup.Provider registries, CompoundTag nbt) {
    NonNullList<ItemStack> loaded = NonNullList.withSize(stacks.size(), ItemStack.EMPTY);
    ContainerHelper.loadAllItems(nbt, loaded, registries);
    stacks = loaded;
  }

  protected void onContentsChanged(int slot) {}
}
