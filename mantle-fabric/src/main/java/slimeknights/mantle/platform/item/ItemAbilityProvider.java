package slimeknights.mantle.platform.item;

import net.minecraft.world.item.ItemStack;

/** Implement on an {@link net.minecraft.world.item.Item} to advertise abilities */
public interface ItemAbilityProvider {
  /** Checks if the stack can perform the given ability */
  boolean canPerformAction(ItemStack stack, ItemAbility ability);
}
