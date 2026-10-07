package slimeknights.mantle.platform.item;

import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;

/** Replacement for NeoForge's item handler capability, looking up Fabric Transfer API storages and adapting them. */
public final class ItemHandlers {
  private ItemHandlers() {}

  /** Gets the item handler of a block, or null if there is none */
  @Nullable
  public static IItemHandler getBlock(Level level, BlockPos pos, @Nullable Direction side) {
    Storage<ItemVariant> storage = ItemStorage.SIDED.find(level, pos, side);
    return storage == null ? null : new ItemStorageAdapter(storage);
  }

  /** Adapts an item storage. Slot arguments are ignored for inserts, and used to find the resource for extracts. */
  private static class ItemStorageAdapter implements IItemHandler {
    private final Storage<ItemVariant> storage;

    private ItemStorageAdapter(Storage<ItemVariant> storage) {
      this.storage = storage;
    }

    private java.util.List<net.fabricmc.fabric.api.transfer.v1.storage.StorageView<ItemVariant>> views() {
      java.util.List<net.fabricmc.fabric.api.transfer.v1.storage.StorageView<ItemVariant>> list = new java.util.ArrayList<>();
      try (net.fabricmc.fabric.api.transfer.v1.transaction.Transaction tx = net.fabricmc.fabric.api.transfer.v1.transaction.Transaction.openOuter()) {
        for (var view : storage) {
          list.add(view);
        }
      }
      return list;
    }

    @Override
    public int getSlots() {
      return views().size();
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
      var views = views();
      if (slot < 0 || slot >= views.size()) {
        return ItemStack.EMPTY;
      }
      var view = views.get(slot);
      return view.isResourceBlank() ? ItemStack.EMPTY : view.getResource().toStack((int) Math.min(Integer.MAX_VALUE, view.getAmount()));
    }

    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
      if (stack.isEmpty()) {
        return stack;
      }
      try (var tx = net.fabricmc.fabric.api.transfer.v1.transaction.Transaction.openOuter()) {
        long inserted = storage.insert(ItemVariant.of(stack), stack.getCount(), tx);
        if (!simulate) {
          tx.commit();
        }
        return stack.copyWithCount(stack.getCount() - (int) inserted);
      }
    }

    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
      var views = views();
      if (slot < 0 || slot >= views.size() || views.get(slot).isResourceBlank()) {
        return ItemStack.EMPTY;
      }
      ItemVariant variant = views.get(slot).getResource();
      try (var tx = net.fabricmc.fabric.api.transfer.v1.transaction.Transaction.openOuter()) {
        long extracted = storage.extract(variant, amount, tx);
        if (!simulate) {
          tx.commit();
        }
        return extracted <= 0 ? ItemStack.EMPTY : variant.toStack((int) extracted);
      }
    }

    @Override
    public int getSlotLimit(int slot) {
      var views = views();
      return slot < 0 || slot >= views.size() ? 0 : (int) Math.min(Integer.MAX_VALUE, views.get(slot).getCapacity());
    }

    @Override
    public boolean isItemValid(int slot, ItemStack stack) {
      try (var tx = net.fabricmc.fabric.api.transfer.v1.transaction.Transaction.openOuter()) {
        return storage.simulateInsert(ItemVariant.of(stack), 1, tx) > 0;
      }
    }
  }
}
