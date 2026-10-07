package slimeknights.mantle.platform.item;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/** Menu slot backed by an item handler, replacing NeoForge's {@code SlotItemHandler}. */
public class SlotItemHandler extends Slot {
  private static final Container EMPTY_INVENTORY = new net.minecraft.world.SimpleContainer(0);
  private final IItemHandler itemHandler;
  private final int index;

  public SlotItemHandler(IItemHandler itemHandler, int index, int x, int y) {
    super(EMPTY_INVENTORY, index, x, y);
    this.itemHandler = itemHandler;
    this.index = index;
  }

  public IItemHandler getItemHandler() {
    return itemHandler;
  }

  @Override
  public boolean mayPlace(ItemStack stack) {
    return !stack.isEmpty() && itemHandler.isItemValid(index, stack);
  }

  @Override
  public ItemStack getItem() {
    return itemHandler.getStackInSlot(index);
  }

  @Override
  public void set(ItemStack stack) {
    if (itemHandler instanceof IItemHandlerModifiable modifiable) {
      modifiable.setStackInSlot(index, stack);
    }
    setChanged();
  }

  @Override
  public void initialize(ItemStack stack) {
    set(stack);
  }

  @Override
  public void onQuickCraft(ItemStack oldStack, ItemStack newStack) {}

  @Override
  public int getMaxStackSize() {
    return itemHandler.getSlotLimit(index);
  }

  @Override
  public int getMaxStackSize(ItemStack stack) {
    ItemStack maxAdd = stack.copyWithCount(stack.getMaxStackSize());
    ItemStack remainder = itemHandler.insertItem(index, maxAdd, true);
    return stack.getMaxStackSize() - remainder.getCount() + itemHandler.getStackInSlot(index).getCount();
  }

  @Override
  public boolean mayPickup(Player player) {
    return !itemHandler.extractItem(index, 1, true).isEmpty();
  }

  @Override
  public ItemStack remove(int amount) {
    return itemHandler.extractItem(index, amount, false);
  }
}
