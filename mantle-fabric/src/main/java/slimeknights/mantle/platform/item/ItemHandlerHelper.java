package slimeknights.mantle.platform.item;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/** Item handler helpers, replacing Forge's {@code ItemHandlerHelper} */
public final class ItemHandlerHelper {
  private ItemHandlerHelper() {}

  public static boolean canItemStacksStack(ItemStack a, ItemStack b) {
    return !a.isEmpty() && ItemStack.isSameItemSameComponents(a, b);
  }

  public static ItemStack copyStackWithSize(ItemStack stack, int size) {
    return size <= 0 ? ItemStack.EMPTY : stack.copyWithCount(size);
  }

  /** Inserts the stack into the handler, trying each slot, returning the remainder */
  public static ItemStack insertItem(IItemHandler dest, ItemStack stack, boolean simulate) {
    if (dest == null || stack.isEmpty()) {
      return stack;
    }
    for (int i = 0; i < dest.getSlots(); i++) {
      stack = dest.insertItem(i, stack, simulate);
      if (stack.isEmpty()) {
        return ItemStack.EMPTY;
      }
    }
    return stack;
  }

  /** Gives the stack to the player, dropping what does not fit */
  public static void giveItemToPlayer(Player player, ItemStack stack, int preferredSlot) {
    if (stack.isEmpty()) {
      return;
    }
    net.minecraft.world.entity.player.Inventory inventory = player.getInventory();
    if (preferredSlot >= 0 && preferredSlot < inventory.items.size() && inventory.items.get(preferredSlot).isEmpty()) {
      inventory.items.set(preferredSlot, stack);
      return;
    }
    giveItemToPlayer(player, stack);
  }

  public static void giveItemToPlayer(Player player, ItemStack stack) {
    if (stack.isEmpty()) {
      return;
    }
    if (player.getInventory().add(stack)) {
      player.level().playSound(null, player.getX(), player.getY() + 0.5, player.getZ(), SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.2F, ((player.getRandom().nextFloat() - player.getRandom().nextFloat()) * 0.7F + 1.0F) * 2.0F);
    }
    if (!stack.isEmpty() && !player.level().isClientSide) {
      ItemEntity entity = new ItemEntity(player.level(), player.getX(), player.getY() + 0.5, player.getZ(), stack);
      entity.setPickUpDelay(40);
      entity.setDeltaMovement(entity.getDeltaMovement().multiply(0, 1, 0));
      player.level().addFreshEntity(entity);
    }
  }
}
