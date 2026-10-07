package slimeknights.tconstruct.gadgets.capability;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import slimeknights.mantle.platform.event.EventBus;
import slimeknights.mantle.platform.capability.Capability;
import slimeknights.mantle.platform.capability.RegisterCapabilitiesEvent;
import slimeknights.mantle.platform.event.AttachCapabilitiesEvent;
import slimeknights.mantle.platform.event.EventPriority;
import slimeknights.tconstruct.TConstruct;

/** Capability logic */
public class PiggybackCapability {
  private static final ResourceLocation ID = TConstruct.getResource("piggyback");
  public static final Capability<PiggybackHandler> PIGGYBACK = new Capability<>("piggyback");

  private PiggybackCapability() {}

  /** Registers this capability */
  public static void register() {
    EventBus.MOD_BUS.addListener(EventPriority.NORMAL, false, RegisterCapabilitiesEvent.class, PiggybackCapability::register);
    EventBus.BUS.addGenericListener(Entity.class, PiggybackCapability::attachCapability);
  }

  /** Registers the capability with the event bus */
  private static void register(RegisterCapabilitiesEvent event) {
    event.register(PiggybackHandler.class);
  }

  /** Event listener to attach the capability */
  private static void attachCapability(AttachCapabilitiesEvent<Entity> event) {
    if (event.getObject() instanceof Player) {
      event.addCapability(ID, new PiggybackHandler((Player) event.getObject()));
    }
  }
}
