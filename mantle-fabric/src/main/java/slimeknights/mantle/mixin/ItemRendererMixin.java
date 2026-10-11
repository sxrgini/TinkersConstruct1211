package slimeknights.mantle.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.entity.ItemRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Vanilla ignores the vertex colors baked into item quads, but Mantle models (fluid in tanks and cans, colored blocks) use them for color.
 * Multiplies the vertex color of the quad into the render color to match the behavior on NeoForge.
 */
@Mixin(ItemRenderer.class)
public class ItemRendererMixin {
  @WrapOperation(
    method = "renderQuadList",
    at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/VertexConsumer;putBulkData(Lcom/mojang/blaze3d/vertex/PoseStack$Pose;Lnet/minecraft/client/renderer/block/model/BakedQuad;FFFFII)V"))
  private void mantle$applyVertexColor(VertexConsumer consumer, PoseStack.Pose pose, BakedQuad quad, float red, float green, float blue, float alpha, int light, int overlay, Operation<Void> original) {
    int abgr = quad.getVertices()[3];
    if (abgr != -1) {
      red *= (abgr & 0xFF) / 255f;
      green *= ((abgr >> 8) & 0xFF) / 255f;
      blue *= ((abgr >> 16) & 0xFF) / 255f;
    }
    original.call(consumer, pose, quad, red, green, blue, alpha, light, overlay);
  }
}
