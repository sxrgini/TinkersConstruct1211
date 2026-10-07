package slimeknights.mantle;

import net.fabricmc.fabric.api.entity.FakePlayer;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import slimeknights.mantle.datagen.MantleTags;
import slimeknights.mantle.registration.MantleData;

import java.util.ArrayList;
import java.util.List;

/** Handles events for any Mantle driven logic. */
public class MantleEvents {
  private MantleEvents() {}

  /** Registers all Fabric event listeners */
  public static void init() {
    ServerLivingEntityEvents.ALLOW_DEATH.register(MantleEvents::onLivingDeath);
    ServerPlayerEvents.COPY_FROM.register(MantleEvents::onPlayerClone);
  }

  /* Soulbound. Items are kept out of the drops by InventoryMixin, then moved to the new player on clone */

  /** Called when the player dies to store the slot to return items into */
  private static boolean onLivingDeath(LivingEntity entity, DamageSource source, float amount) {
    // this is the latest we can add slot markers to the items so we can return them to slots
    if (!entity.level().getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY) && entity instanceof Player player && !(player instanceof FakePlayer)) {
      Inventory inventory = player.getInventory();

      // just iterate the whole inventory, no slot specific behavior
      int totalSize = inventory.getContainerSize();
      for (int i = 0; i < totalSize; i++) {
        ItemStack stack = inventory.getItem(i);
        if (!stack.isEmpty() && stack.is(MantleTags.Items.SOULBOUND)) {
          stack.set(MantleData.SOULBOUND_SLOT, i);
        }
      }
    }
    return true;
  }

  /** Called when the new player is created to fetch the soulbound item from the old */
  private static void onPlayerClone(ServerPlayer original, ServerPlayer clone, boolean alive) {
    if (alive) {
      return;
    }
    // inventory already copied
    if (clone.level().getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY) || original.isSpectator()) {
      return;
    }
    // find items with the soulbound tag set and move them over
    Inventory originalInv = original.getInventory();
    Inventory cloneInv = clone.getInventory();
    int size = Math.min(originalInv.getContainerSize(), cloneInv.getContainerSize()); // not needed probably, but might as well be safe
    List<ItemStack> takenSlot = new ArrayList<>();
    for(int i = 0; i < size; i++) {
      ItemStack stack = originalInv.getItem(i);
      if (!stack.isEmpty()) {
        int slot = stack.getOrDefault(MantleData.SOULBOUND_SLOT, -1);
        if (slot != -1) {
          if (cloneInv.getItem(i).isEmpty()) {
            cloneInv.setItem(i, stack);
          } else {
            takenSlot.add(stack);
          }
          // remove the slot component
          stack.remove(MantleData.SOULBOUND_SLOT);
        }
      }
    }

    // handle items that did not get their requested slot last, to ensure they don't take someone else's slot while being added to a default
    for (ItemStack stack : takenSlot) {
      if (!cloneInv.add(stack)) {
        // last resort, somehow we just cannot put the stack anywhere, so drop it on the ground
        // this should never happen, but better to be safe
        clone.drop(stack, false);
      }
    }
  }
}
