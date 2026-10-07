package slimeknights.mantle.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import slimeknights.mantle.platform.event.EventBus;
import slimeknights.mantle.platform.event.client.RenderLivingEvent;

/** Fires {@link RenderLivingEvent} before and after living entities render */
@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin {
  @Inject(method = "render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", at = @At("HEAD"), cancellable = true)
  private void mantle$renderPre(LivingEntity entity, float yaw, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int light, CallbackInfo ci) {
    EventBus bus = EventBus.BUS;
    if (bus.hasListeners(RenderLivingEvent.Pre.class) && bus.post(new RenderLivingEvent.Pre(entity, (LivingEntityRenderer<?,?>) (Object) this, partialTick, poseStack, buffer, light))) {
      ci.cancel();
    }
  }

  @Inject(method = "render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", at = @At("TAIL"))
  private void mantle$renderPost(LivingEntity entity, float yaw, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int light, CallbackInfo ci) {
    EventBus bus = EventBus.BUS;
    if (bus.hasListeners(RenderLivingEvent.Post.class)) {
      bus.post(new RenderLivingEvent.Post(entity, (LivingEntityRenderer<?,?>) (Object) this, partialTick, poseStack, buffer, light));
    }
  }
}
