package slimeknights.mantle.platform.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/** Replacements for NeoForge extensions on {@link ItemStack} */
public final class ItemHelpers {
  private ItemHelpers() {}

  /** Gets the stack left behind after crafting with the given stack, replacing NeoForge's {@code ItemStack#getCraftingRemainingItem} */
  public static ItemStack getCraftingRemainingItem(ItemStack stack) {
    Item remaining = stack.getItem().getCraftingRemainingItem();
    return remaining == null ? ItemStack.EMPTY : new ItemStack(remaining);
  }
}
