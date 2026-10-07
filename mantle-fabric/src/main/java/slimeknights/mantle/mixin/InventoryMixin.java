package slimeknights.mantle.mixin;

import net.fabricmc.fabric.api.entity.FakePlayer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import slimeknights.mantle.registration.MantleData;

import java.util.HashMap;
import java.util.Map;

/** Keeps soulbound items out of the death drops. Replaces NeoForge's {@code LivingDropsEvent} handling, the items are moved to the new player on respawn. */
@Mixin(Inventory.class)
public abstract class InventoryMixin {
  @Shadow @Final public Player player;
  @Shadow public abstract ItemStack getItem(int slot);
  @Shadow public abstract void setItem(int slot, ItemStack stack);
  @Shadow public abstract int getContainerSize();

  @Unique
  private final Map<Integer,ItemStack> mantle$soulbound = new HashMap<>();

  @Inject(method = "dropAll", at = @At("HEAD"))
  private void mantle$hideSoulbound(CallbackInfo ci) {
    if (!(player instanceof ServerPlayer) || player instanceof FakePlayer || player.level().getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY)) {
      return;
    }
    int size = getContainerSize();
    for (int i = 0; i < size; i++) {
      ItemStack stack = getItem(i);
      if (!stack.isEmpty() && stack.has(MantleData.SOULBOUND_SLOT)) {
        mantle$soulbound.put(i, stack);
        setItem(i, ItemStack.EMPTY);
      }
    }
  }

  @Inject(method = "dropAll", at = @At("RETURN"))
  private void mantle$restoreSoulbound(CallbackInfo ci) {
    mantle$soulbound.forEach(this::setItem);
    mantle$soulbound.clear();
  }
}
