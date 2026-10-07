package slimeknights.mantle.platform.event.lifecycle;

import slimeknights.mantle.platform.event.Event;
import slimeknights.mantle.platform.event.EventBus;

import java.util.function.Supplier;

/** Stand in for Forge's ModLoader, posts addon facing events on the mod bus */
public final class ModLoader {
  private static final ModLoader INSTANCE = new ModLoader();

  private ModLoader() {}

  public static ModLoader get() {
    return INSTANCE;
  }

  /** Creates and posts an event on the mod bus */
  public void runEventGenerator(Supplier<? extends Event> generator) {
    EventBus.MOD_BUS.post(generator.get());
  }

  /** Posts an event on the mod bus */
  public void postEvent(Event event) {
    EventBus.MOD_BUS.post(event);
  }
}
