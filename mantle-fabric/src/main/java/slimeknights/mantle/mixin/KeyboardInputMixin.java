package slimeknights.mantle.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.KeyboardInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import slimeknights.mantle.platform.event.EventBus;
import slimeknights.mantle.platform.event.client.MovementInputUpdateEvent;

/** Fires {@link MovementInputUpdateEvent} after the local player's input is updated */
@Mixin(KeyboardInput.class)
public class KeyboardInputMixin {
  @Inject(method = "tick", at = @At("TAIL"))
  private void mantle$movementInput(boolean sneaking, float sneakingSpeed, CallbackInfo ci) {
    EventBus bus = EventBus.BUS;
    if (Minecraft.getInstance().player != null && bus.hasListeners(MovementInputUpdateEvent.class)) {
      bus.post(new MovementInputUpdateEvent(Minecraft.getInstance().player, (KeyboardInput) (Object) this));
    }
  }
}
