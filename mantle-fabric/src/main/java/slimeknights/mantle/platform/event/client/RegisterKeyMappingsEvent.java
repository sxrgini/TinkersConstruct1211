package slimeknights.mantle.platform.event.client;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import slimeknights.mantle.platform.event.Event;
import slimeknights.mantle.platform.event.lifecycle.IModBusEvent;

/** Registers key mappings, backed by Fabric's key binding helper */
public class RegisterKeyMappingsEvent extends Event implements IModBusEvent {
  public void register(KeyMapping mapping) {
    KeyBindingHelper.registerKeyBinding(mapping);
  }
}
