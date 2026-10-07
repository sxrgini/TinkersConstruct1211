package slimeknights.mantle.platform.event.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import slimeknights.mantle.platform.event.Event;

/** Fired when an entity name tag renders, vanilla does not fire this but custom renderers may */
@Event.HasResult
public class RenderNameTagEvent extends Event {
  private final Entity entity;
  private Component content;
  private final EntityRenderer<?> renderer;
  private final PoseStack poseStack;
  private final MultiBufferSource buffer;
  private final int packedLight;
  private final float partialTick;

  public RenderNameTagEvent(Entity entity, Component content, EntityRenderer<?> renderer, PoseStack poseStack, MultiBufferSource buffer, int packedLight, float partialTick) {
    this.entity = entity;
    this.content = content;
    this.renderer = renderer;
    this.poseStack = poseStack;
    this.buffer = buffer;
    this.packedLight = packedLight;
    this.partialTick = partialTick;
  }

  public Entity getEntity() { return entity; }
  public Component getContent() { return content; }
  public void setContent(Component content) { this.content = content; }
  public EntityRenderer<?> getEntityRenderer() { return renderer; }
  public PoseStack getPoseStack() { return poseStack; }
  public MultiBufferSource getMultiBufferSource() { return buffer; }
  public int getPackedLight() { return packedLight; }
  public float getPartialTick() { return partialTick; }
}
