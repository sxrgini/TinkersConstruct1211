package slimeknights.mantle.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import slimeknights.mantle.platform.event.EventBus;
import slimeknights.mantle.platform.event.client.RenderHandEvent;

/** Fires {@link RenderHandEvent} before a hand renders in first person */
@Mixin(ItemInHandRenderer.class)
public class ItemInHandRendererMixin {
  @Inject(method = "renderArmWithItem", at = @At("HEAD"), cancellable = true)
  private void mantle$renderHand(AbstractClientPlayer player, float partialTick, float pitch, InteractionHand hand, float swingProgress, ItemStack stack, float equipProgress, PoseStack poseStack, MultiBufferSource buffer, int light, CallbackInfo ci) {
    EventBus bus = EventBus.BUS;
    if (bus.hasListeners(RenderHandEvent.class) && bus.post(new RenderHandEvent(hand, poseStack, buffer, light, partialTick, pitch, swingProgress, equipProgress, stack))) {
      ci.cancel();
    }
  }
}
