package slimeknights.mantle.platform.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/** Client behavior of an item, equivalent of Forge's IClientItemExtensions. Applied by {@link ClientExtensions}. */
public interface IClientItemExtensions {
  /** Default instance, changes nothing */
  IClientItemExtensions DEFAULT = new IClientItemExtensions() {};

  /** Gets the extensions for the stack */
  static IClientItemExtensions of(ItemStack stack) {
    return ClientExtensions.of(stack);
  }

  /** Gets a model to render in place of the vanilla armor model, return the original to use vanilla behavior */
  default Model getGenericArmorModel(LivingEntity living, ItemStack stack, EquipmentSlot slot, HumanoidModel<?> original) {
    return original;
  }

  /** Same as {@link #getGenericArmorModel} for humanoid only models */
  default HumanoidModel<?> getHumanoidArmorModel(LivingEntity living, ItemStack stack, EquipmentSlot slot, HumanoidModel<?> original) {
    return original;
  }

  /** Applies a custom transform for first person hand rendering, return true to skip the vanilla transform */
  default boolean applyForgeHandTransform(PoseStack poseStack, LocalPlayer player, HumanoidArm arm, ItemStack stack, float partialTicks, float equipProgress, float swingProgress) {
    return false;
  }
}
