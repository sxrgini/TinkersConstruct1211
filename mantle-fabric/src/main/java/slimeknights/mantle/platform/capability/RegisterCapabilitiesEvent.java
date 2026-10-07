package slimeknights.mantle.platform.capability;

import slimeknights.mantle.platform.event.Event;
import slimeknights.mantle.platform.event.lifecycle.IModBusEvent;

/** Event where Forge capability types were registered. Capability types are plain classes here, so registering does nothing. */
public class RegisterCapabilitiesEvent extends Event implements IModBusEvent {
  public void register(Class<?> type) {}
}
