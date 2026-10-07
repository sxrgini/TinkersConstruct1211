package slimeknights.tconstruct.world.client;

import net.minecraft.util.FastColor;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.PiglinHeadModel;
import net.minecraft.client.model.geom.ModelPart;

/** Extension of {@link PiglinHeadModel} to adjust scale for slimeskulls */
public class PiglinSkullModel extends PiglinHeadModel {
  public PiglinSkullModel(ModelPart pRoot) {
    super(pRoot);
  }

  @Override
  public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int light, int overlay, int color) {
    float alpha = FastColor.ARGB32.alpha(color) / 255f;
    float red = FastColor.ARGB32.red(color) / 255f;
    float green = FastColor.ARGB32.green(color) / 255f;
    float blue = FastColor.ARGB32.blue(color) / 255f;
    poseStack.pushPose();
    poseStack.scale(0.97f, 0.97f, 0.97f);
    super.renderToBuffer(poseStack, buffer, light, overlay, FastColor.ARGB32.colorFromFloat(alpha, red, green, blue));
    poseStack.popPose();
  }
}
