package slimeknights.mantle.platform.event.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemFrameRenderer;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.item.ItemStack;
import slimeknights.mantle.platform.event.Event;

/** Fired when an item frame renders its item, vanilla does not fire this but custom renderers may */
@Event.Cancelable
public class RenderItemInFrameEvent extends Event {
  private final ItemFrame frame;
  private final ItemFrameRenderer<?> renderer;
  private final PoseStack poseStack;
  private final MultiBufferSource buffer;
  private final int packedLight;

  public RenderItemInFrameEvent(ItemFrame frame, ItemFrameRenderer<?> renderer, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
    this.frame = frame;
    this.renderer = renderer;
    this.poseStack = poseStack;
    this.buffer = buffer;
    this.packedLight = packedLight;
  }

  public ItemStack getItemStack() { return frame.getItem(); }
  public ItemFrame getEntity() { return frame; }
  public ItemFrameRenderer<?> getRenderer() { return renderer; }
  public PoseStack getPoseStack() { return poseStack; }
  public MultiBufferSource getMultiBufferSource() { return buffer; }
  public int getPackedLight() { return packedLight; }
}
