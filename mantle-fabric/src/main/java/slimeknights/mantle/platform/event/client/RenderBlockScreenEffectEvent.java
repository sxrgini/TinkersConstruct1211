package slimeknights.mantle.platform.event.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import slimeknights.mantle.platform.event.Event;

/** Fired when a block overlay renders on the screen while inside a block, fired by {@code ScreenEffectRendererMixin} */
@Event.Cancelable
public class RenderBlockScreenEffectEvent extends Event {
  private final LocalPlayer player;
  private final PoseStack poseStack;
  private final BlockState state;
  private final BlockPos pos;

  public RenderBlockScreenEffectEvent(LocalPlayer player, PoseStack poseStack, BlockState state, BlockPos pos) {
    this.player = player;
    this.poseStack = poseStack;
    this.state = state;
    this.pos = pos;
  }

  public LocalPlayer getPlayer() { return player; }
  public PoseStack getPoseStack() { return poseStack; }
  public BlockState getBlockState() { return state; }
  public BlockPos getBlockPos() { return pos; }
}
