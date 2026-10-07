package slimeknights.mantle.mixin;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import slimeknights.mantle.client.ClientEvents;

/** Replaces the vanilla health bar with Mantle's when enabled in the config */
@Mixin(Gui.class)
public class GuiMixin {
  @Inject(method = "renderPlayerHealth", at = @At("HEAD"), cancellable = true)
  private void mantle$renderHealth(GuiGraphics graphics, CallbackInfo ci) {
    if (ClientEvents.HEARTS.renderHealthbar(graphics)) {
      ci.cancel();
    }
  }
}
