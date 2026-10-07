package slimeknights.mantle.platform.event.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import slimeknights.mantle.platform.event.Event;

/** Fired before a hand is rendered in first person, fired by {@code ItemInHandRendererMixin} */
@Event.Cancelable
public class RenderHandEvent extends Event {
  private final InteractionHand hand;
  private final PoseStack poseStack;
  private final MultiBufferSource buffer;
  private final int packedLight;
  private final float partialTick;
  private final float interpolatedPitch;
  private final float swingProgress;
  private final float equipProgress;
  private final ItemStack stack;

  public RenderHandEvent(InteractionHand hand, PoseStack poseStack, MultiBufferSource buffer, int packedLight, float partialTick, float interpolatedPitch, float swingProgress, float equipProgress, ItemStack stack) {
    this.hand = hand;
    this.poseStack = poseStack;
    this.buffer = buffer;
    this.packedLight = packedLight;
    this.partialTick = partialTick;
    this.interpolatedPitch = interpolatedPitch;
    this.swingProgress = swingProgress;
    this.equipProgress = equipProgress;
    this.stack = stack;
  }

  public InteractionHand getHand() { return hand; }
  public PoseStack getPoseStack() { return poseStack; }
  public MultiBufferSource getMultiBufferSource() { return buffer; }
  public int getPackedLight() { return packedLight; }
  public float getPartialTick() { return partialTick; }
  public float getInterpolatedPitch() { return interpolatedPitch; }
  public float getSwingProgress() { return swingProgress; }
  public float getEquipProgress() { return equipProgress; }
  public ItemStack getItemStack() { return stack; }
}
