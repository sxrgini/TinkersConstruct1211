package slimeknights.mantle.platform.event.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.phys.BlockHitResult;
import slimeknights.mantle.platform.event.Event;

/** Fired before the block selection outline renders, bridged to Fabric's block outline event */
@Event.Cancelable
public abstract class RenderHighlightEvent extends Event {
  private final LevelRenderer renderer;
  private final Camera camera;
  private final float partialTick;
  private final PoseStack poseStack;
  private final MultiBufferSource buffers;

  protected RenderHighlightEvent(LevelRenderer renderer, Camera camera, float partialTick, PoseStack poseStack, MultiBufferSource buffers) {
    this.renderer = renderer;
    this.camera = camera;
    this.partialTick = partialTick;
    this.poseStack = poseStack;
    this.buffers = buffers;
  }

  public LevelRenderer getLevelRenderer() { return renderer; }
  public Camera getCamera() { return camera; }
  public float getPartialTick() { return partialTick; }
  public PoseStack getPoseStack() { return poseStack; }
  public MultiBufferSource getMultiBufferSource() { return buffers; }

  /** Highlight of a block */
  public static class Block extends RenderHighlightEvent {
    private final BlockHitResult target;

    public Block(LevelRenderer renderer, Camera camera, BlockHitResult target, float partialTick, PoseStack poseStack, MultiBufferSource buffers) {
      super(renderer, camera, partialTick, poseStack, buffers);
      this.target = target;
    }

    public BlockHitResult getTarget() { return target; }
  }
}
