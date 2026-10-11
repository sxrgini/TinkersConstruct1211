package slimeknights.mantle.mixin;

import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.Shadow;
import slimeknights.mantle.Mantle;

import java.util.List;

/**
 * If the server and client disagree on the number of slots in a menu, vanilla throws while applying the full content packet which disconnects the player.
 * Truncating the extra stacks keeps the player in the game, the menu will still be out of sync but resyncing is better than a crash.
 */
@Mixin(AbstractContainerMenu.class)
public abstract class AbstractContainerMenuMixin {
  @Shadow @org.spongepowered.asm.mixin.Final public net.minecraft.core.NonNullList<net.minecraft.world.inventory.Slot> slots;

  @ModifyVariable(method = "initializeContents", at = @At("HEAD"), argsOnly = true)
  private List<ItemStack> mantle$truncateContents(List<ItemStack> items) {
    if (items.size() > slots.size()) {
      Mantle.logger.warn("Server sent {} stacks for menu {} that only has {} slots, ignoring the extras", items.size(), getClass().getName(), slots.size());
      return items.subList(0, slots.size());
    }
    return items;
  }
}
