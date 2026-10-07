package slimeknights.mantle.platform.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.ArmorRenderer;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.Map;

/** Registry of client extensions for items and effects, filled from the provider interfaces */
@Environment(EnvType.CLIENT)
public final class ClientExtensions {
  private ClientExtensions() {}

  private static final Map<Item,IClientItemExtensions> ITEMS = new HashMap<>();
  private static final Map<MobEffect,IClientMobEffectExtensions> EFFECTS = new HashMap<>();

  /** Gets the extensions for the item stack */
  public static IClientItemExtensions of(ItemStack stack) {
    return ITEMS.getOrDefault(stack.getItem(), IClientItemExtensions.DEFAULT);
  }

  /** Gets the extensions for the effect */
  public static IClientMobEffectExtensions of(MobEffectInstance instance) {
    return EFFECTS.getOrDefault(instance.getEffect().value(), IClientMobEffectExtensions.DEFAULT);
  }

  /** Collects extensions from all registered items and effects, call once on the client after registries are filled */
  public static void init() {
    for (Item item : BuiltInRegistries.ITEM) {
      if (item instanceof IClientItemExtensionsProvider provider) {
        provider.initializeClient(extensions -> {
          ITEMS.put(item, extensions);
          if (item instanceof ArmorItem armor) {
            ArmorRenderer.register((poseStack, buffers, stack, entity, slot, light, contextModel) -> {
              Model model = extensions.getGenericArmorModel(entity, stack, slot, contextModel);
              if (model != contextModel) {
                model.renderToBuffer(poseStack, buffers.getBuffer(net.minecraft.client.renderer.RenderType.armorCutoutNoCull(net.minecraft.resources.ResourceLocation.withDefaultNamespace("textures/misc/white.png"))), light, OverlayTexture.NO_OVERLAY, -1);
              }
            }, armor);
          }
        });
      }
    }
    for (MobEffect effect : BuiltInRegistries.MOB_EFFECT) {
      if (effect instanceof IClientMobEffectExtensionsProvider provider) {
        provider.initializeClient(extensions -> EFFECTS.put(effect, extensions));
      }
    }
  }
}
