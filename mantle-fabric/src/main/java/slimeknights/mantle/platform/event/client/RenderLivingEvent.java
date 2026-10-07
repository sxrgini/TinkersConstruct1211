package slimeknights.mantle.platform.event.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.LivingEntity;
import slimeknights.mantle.platform.event.Event;

/** Fired around the rendering of living entities, fired by {@code LivingEntityRendererMixin} */
public abstract class RenderLivingEvent extends Event {
  private final LivingEntity entity;
  private final LivingEntityRenderer<?,?> renderer;
  private final float partialTick;
  private final PoseStack poseStack;
  private final MultiBufferSource buffer;
  private final int packedLight;

  protected RenderLivingEvent(LivingEntity entity, LivingEntityRenderer<?,?> renderer, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
    this.entity = entity;
    this.renderer = renderer;
    this.partialTick = partialTick;
    this.poseStack = poseStack;
    this.buffer = buffer;
    this.packedLight = packedLight;
  }

  public LivingEntity getEntity() { return entity; }
  public LivingEntityRenderer<?,?> getRenderer() { return renderer; }
  public float getPartialTick() { return partialTick; }
  public PoseStack getPoseStack() { return poseStack; }
  public MultiBufferSource getMultiBufferSource() { return buffer; }
  public int getPackedLight() { return packedLight; }

  /** Fired before the entity renders */
  @Cancelable
  public static class Pre extends RenderLivingEvent {
    public Pre(LivingEntity entity, LivingEntityRenderer<?,?> renderer, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
      super(entity, renderer, partialTick, poseStack, buffer, packedLight);
    }
  }

  /** Fired after the entity renders */
  public static class Post extends RenderLivingEvent {
    public Post(LivingEntity entity, LivingEntityRenderer<?,?> renderer, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
      super(entity, renderer, partialTick, poseStack, buffer, packedLight);
    }
  }
}
