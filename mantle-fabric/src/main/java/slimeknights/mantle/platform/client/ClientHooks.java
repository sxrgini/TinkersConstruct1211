package slimeknights.mantle.platform.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/** Replacement for Forge's ForgeHooksClient helpers */
public final class ClientHooks {
  private ClientHooks() {}

  /** Gets the model to use for armor rendering from the item's client extensions */
  public static Model getArmorModel(LivingEntity entity, ItemStack stack, EquipmentSlot slot, HumanoidModel<?> original) {
    return ClientExtensions.of(stack).getGenericArmorModel(entity, stack, slot, original);
  }

  /** Gets the armor texture path, there is no extension point on Fabric so the path is unchanged */
  public static String getArmorTexture(Entity entity, ItemStack stack, String path, EquipmentSlot slot, String type) {
    return path;
  }

  /** Applies the display transform of the model for the given context and returns the model */
  public static BakedModel handleCameraTransforms(PoseStack poseStack, BakedModel model, ItemDisplayContext context, boolean leftHand) {
    model.getTransforms().getTransform(context).apply(leftHand, poseStack);
    return model;
  }
}
