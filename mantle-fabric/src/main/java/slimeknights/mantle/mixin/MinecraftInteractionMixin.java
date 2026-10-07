package slimeknights.mantle.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import slimeknights.mantle.platform.event.EventBus;
import slimeknights.mantle.platform.event.client.InputEvent;

/** Fires {@link InputEvent.InteractionKeyMappingTriggered} for attack, use and pick block */
@Mixin(Minecraft.class)
public abstract class MinecraftInteractionMixin {
  @Shadow
  public net.minecraft.client.Options options;
  @Shadow
  public net.minecraft.client.player.LocalPlayer player;

  @Unique
  private boolean mantle$fire(int button, KeyMapping mapping, InteractionHand hand) {
    EventBus bus = EventBus.BUS;
    if (!bus.hasListeners(InputEvent.InteractionKeyMappingTriggered.class)) {
      return false;
    }
    InputEvent.InteractionKeyMappingTriggered event = new InputEvent.InteractionKeyMappingTriggered(button, mapping, hand);
    if (bus.post(event)) {
      if (event.shouldSwingHand() && player != null) {
        player.swing(hand);
      }
      return true;
    }
    return false;
  }

  @Inject(method = "startAttack", at = @At("HEAD"), cancellable = true)
  private void mantle$attack(CallbackInfoReturnable<Boolean> cir) {
    if (mantle$fire(0, options.keyAttack, InteractionHand.MAIN_HAND)) {
      cir.setReturnValue(false);
    }
  }

  @Inject(method = "startUseItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;getItemInHand(Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/item/ItemStack;"), cancellable = true)
  private void mantle$use(CallbackInfo ci, @Local InteractionHand hand) {
    if (mantle$fire(1, options.keyUse, hand)) {
      ci.cancel();
    }
  }

  @Inject(method = "pickBlock", at = @At("HEAD"), cancellable = true)
  private void mantle$pick(CallbackInfo ci) {
    if (mantle$fire(2, options.keyPickItem, InteractionHand.MAIN_HAND)) {
      ci.cancel();
    }
  }
}
