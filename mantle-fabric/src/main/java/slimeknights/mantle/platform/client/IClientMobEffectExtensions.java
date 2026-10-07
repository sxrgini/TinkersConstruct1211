package slimeknights.mantle.platform.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.world.effect.MobEffectInstance;

/** Client behavior of a mob effect, equivalent of Forge's IClientMobEffectExtensions. Applied by {@link ClientExtensions}. */
public interface IClientMobEffectExtensions {
  /** Default instance, changes nothing */
  IClientMobEffectExtensions DEFAULT = new IClientMobEffectExtensions() {};

  /** Gets the extensions for the effect instance */
  static IClientMobEffectExtensions of(MobEffectInstance instance) {
    return ClientExtensions.of(instance);
  }

  default boolean isVisibleInInventory(MobEffectInstance instance) {
    return true;
  }

  default boolean isVisibleInGui(MobEffectInstance instance) {
    return true;
  }

  /** Renders a custom icon in the inventory, return true if rendered */
  default boolean renderInventoryIcon(MobEffectInstance instance, EffectRenderingInventoryScreen<?> screen, GuiGraphics graphics, int x, int y, int z) {
    return false;
  }

  /** Renders a custom icon in the gui, return true if rendered */
  default boolean renderGuiIcon(MobEffectInstance instance, net.minecraft.client.gui.Gui gui, GuiGraphics graphics, int x, int y, float z, float alpha) {
    return false;
  }
}
