package slimeknights.mantle.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.ScreenEffectRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import slimeknights.mantle.platform.event.EventBus;
import slimeknights.mantle.platform.event.client.RenderBlockScreenEffectEvent;

/** Fires {@link RenderBlockScreenEffectEvent} before the in-block overlay renders */
@Mixin(ScreenEffectRenderer.class)
public class ScreenEffectRendererMixin {
  @WrapOperation(method = "renderScreenEffect", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/ScreenEffectRenderer;renderTex(Lnet/minecraft/client/renderer/texture/TextureAtlasSprite;Lcom/mojang/blaze3d/vertex/PoseStack;)V"))
  private static void mantle$blockOverlay(TextureAtlasSprite sprite, PoseStack poseStack, Operation<Void> original, @Local(argsOnly = true) Minecraft minecraft, @Local BlockState state) {
    EventBus bus = EventBus.BUS;
    LocalPlayer player = minecraft.player;
    if (player != null && bus.hasListeners(RenderBlockScreenEffectEvent.class)) {
      BlockPos pos = mantle$findPos(player, state);
      if (bus.post(new RenderBlockScreenEffectEvent(player, poseStack, state, pos))) {
        return;
      }
    }
    original.call(sprite, poseStack);
  }

  /** Repeats the vanilla search for the view blocking position, as vanilla only returns the state */
  private static BlockPos mantle$findPos(LocalPlayer player, BlockState state) {
    BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
    for (int i = 0; i < 8; i++) {
      double x = player.getX() + ((i % 2) - 0.5f) * player.getBbWidth() * 0.8f;
      double y = player.getEyeY() + (((i >> 1) % 2) - 0.5f) * 0.1f * player.getScale();
      double z = player.getZ() + (((i >> 2) % 2) - 0.5f) * player.getBbWidth() * 0.8f;
      mutable.set(x, y, z);
      BlockState found = player.level().getBlockState(mutable);
      if (found == state && found.getRenderShape() != RenderShape.INVISIBLE) {
        return mutable.immutable();
      }
    }
    return player.blockPosition();
  }
}
