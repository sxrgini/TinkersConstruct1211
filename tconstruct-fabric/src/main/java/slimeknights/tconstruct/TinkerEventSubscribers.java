package slimeknights.tconstruct;

import slimeknights.mantle.platform.event.EventBus;

/** Registers the static event subscriber classes, replacing Forge's annotation based discovery */
final class TinkerEventSubscribers {
  private TinkerEventSubscribers() {}

  static void registerCommon() {
    EventBus.MOD_BUS.register(slimeknights.tconstruct.common.Sounds.class);
    EventBus.BUS.register(slimeknights.tconstruct.fluids.FluidEvents.class);
    EventBus.BUS.register(slimeknights.tconstruct.shared.CommonsEvents.class);
    EventBus.BUS.register(slimeknights.tconstruct.tools.logic.DoubleJumpHandler.class);
    EventBus.BUS.register(slimeknights.tconstruct.tools.logic.InteractionHandler.class);
    EventBus.BUS.register(slimeknights.tconstruct.tools.logic.ModifierEvents.class);
    EventBus.BUS.register(slimeknights.tconstruct.tools.logic.ToolEvents.class);
    EventBus.BUS.register(slimeknights.tconstruct.world.WorldEvents.class);
  }

  static void registerClient() {
    EventBus.MOD_BUS.register(slimeknights.tconstruct.fluids.FluidClientEvents.class);
    EventBus.MOD_BUS.register(slimeknights.tconstruct.gadgets.GadgetClientEvents.class);
    EventBus.MOD_BUS.register(slimeknights.tconstruct.shared.CommonsClientEvents.class);
    EventBus.BUS.register(slimeknights.tconstruct.shared.TinkerClient.class);
    EventBus.MOD_BUS.register(slimeknights.tconstruct.smeltery.SmelteryClientEvents.class);
    EventBus.MOD_BUS.register(slimeknights.tconstruct.tables.TableClientEvents.class);
    EventBus.MOD_BUS.register(slimeknights.tconstruct.tools.ToolClientEvents.class);
    EventBus.BUS.register(slimeknights.tconstruct.tools.client.ClientInteractionHandler.class);
    EventBus.BUS.register(slimeknights.tconstruct.tools.client.ModifierClientEvents.class);
    EventBus.BUS.register(slimeknights.tconstruct.tools.client.ToolRenderEvents.class);
    EventBus.MOD_BUS.register(slimeknights.tconstruct.world.WorldClientEvents.class);
  }

}
