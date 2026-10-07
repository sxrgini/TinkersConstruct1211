package slimeknights.mantle.platform.event;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

/** Fires bus events for client side Fabric callbacks */
@Environment(EnvType.CLIENT)
public final class FabricClientEventBridge {
  private FabricClientEventBridge() {}

  /** Registers the client callbacks, call from the client initializer */
  public static void init() {
    EventBus bus = EventBus.BUS;
    slimeknights.mantle.platform.event.player.ItemTooltipEvent.init();
    net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
      if (bus.hasListeners(slimeknights.mantle.platform.event.client.ClientPlayerNetworkEvent.LoggingOut.class)) {
        bus.post(new slimeknights.mantle.platform.event.client.ClientPlayerNetworkEvent.LoggingOut(client.gameMode, client.player, handler.getConnection()));
      }
    });
    net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback.EVENT.register((graphics, tickDelta) -> {
      if (bus.hasListeners(slimeknights.mantle.platform.event.client.RenderGuiOverlayEvent.Post.class)) {
        bus.post(new slimeknights.mantle.platform.event.client.RenderGuiOverlayEvent.Post(graphics, tickDelta.getGameTimeDeltaPartialTick(false), slimeknights.mantle.platform.event.client.RenderGuiOverlayEvent.VanillaGuiOverlay.HOTBAR.type()));
      }
    });
    net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents.BLOCK_OUTLINE.register((context, outline) -> {
      if (bus.hasListeners(slimeknights.mantle.platform.event.client.RenderHighlightEvent.Block.class) && net.minecraft.client.Minecraft.getInstance().hitResult instanceof net.minecraft.world.phys.BlockHitResult) {
        net.minecraft.client.renderer.MultiBufferSource buffers = context.consumers();
        if (buffers != null && bus.post(new slimeknights.mantle.platform.event.client.RenderHighlightEvent.Block(context.worldRenderer(), context.camera(), (net.minecraft.world.phys.BlockHitResult) net.minecraft.client.Minecraft.getInstance().hitResult, context.tickCounter().getGameTimeDeltaPartialTick(false), context.matrixStack(), buffers))) {
          return false;
        }
      }
      return true;
    });
    net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents.AFTER_TRANSLUCENT.register(context -> {
      if (bus.hasListeners(slimeknights.mantle.platform.event.client.RenderLevelStageEvent.class)) {
        bus.post(new slimeknights.mantle.platform.event.client.RenderLevelStageEvent(slimeknights.mantle.platform.event.client.RenderLevelStageEvent.Stage.AFTER_TRIPWIRE_BLOCKS, context.worldRenderer(), context.matrixStack(), context.projectionMatrix(), context.tickCounter().getGameTimeDeltaPartialTick(false), context.camera()));
      }
    });
    ClientTickEvents.START_CLIENT_TICK.register(client -> {
      if (client.player != null && bus.hasListeners(TickEvent.PlayerTickEvent.class)) {
        bus.post(new TickEvent.PlayerTickEvent(TickEvent.Phase.START, LogicalSide.CLIENT, client.player));
      }
    });
    ClientTickEvents.END_CLIENT_TICK.register(client -> {
      if (client.player != null && bus.hasListeners(TickEvent.PlayerTickEvent.class)) {
        bus.post(new TickEvent.PlayerTickEvent(TickEvent.Phase.END, LogicalSide.CLIENT, client.player));
      }
    });
  }
}
