package slimeknights.mantle.platform.event;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.world.InteractionResult;
import slimeknights.mantle.platform.event.living.LivingDeathEvent;
import slimeknights.mantle.platform.event.living.LivingEquipmentChangeEvent;
import slimeknights.mantle.platform.event.player.AttackEntityEvent;
import slimeknights.mantle.platform.event.player.PlayerInteractEvent;

/** Fires bus events for things Fabric API already provides callbacks for. Mixins fire the rest, see {@code slimeknights.mantle.mixin}. */
public final class FabricEventBridge {
  private FabricEventBridge() {}

  /** Registers the Fabric callbacks, call during mod initialization */
  public static void init() {
    EventBus bus = EventBus.BUS;

    net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents.START_SERVER_TICK.register(server -> {
      if (bus.hasListeners(TickEvent.PlayerTickEvent.class)) {
        for (net.minecraft.server.level.ServerPlayer player : server.getPlayerList().getPlayers()) {
          bus.post(new TickEvent.PlayerTickEvent(TickEvent.Phase.START, LogicalSide.SERVER, player));
        }
      }
    });
    net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents.END_SERVER_TICK.register(server -> {
      if (bus.hasListeners(TickEvent.PlayerTickEvent.class)) {
        for (net.minecraft.server.level.ServerPlayer player : server.getPlayerList().getPlayers()) {
          bus.post(new TickEvent.PlayerTickEvent(TickEvent.Phase.END, LogicalSide.SERVER, player));
        }
      }
    });

    ServerLivingEntityEvents.ALLOW_DEATH.register((entity, source, amount) ->
      !bus.hasListeners(LivingDeathEvent.class) || !bus.post(new LivingDeathEvent(entity, source)));

    ServerEntityEvents.EQUIPMENT_CHANGE.register((entity, slot, previous, current) -> {
      if (bus.hasListeners(LivingEquipmentChangeEvent.class)) {
        bus.post(new LivingEquipmentChangeEvent(entity, slot, previous, current));
      }
    });

    UseBlockCallback.EVENT.register((player, level, hand, hit) -> {
      if (bus.hasListeners(PlayerInteractEvent.RightClickBlock.class)) {
        PlayerInteractEvent.RightClickBlock event = new PlayerInteractEvent.RightClickBlock(player, hand, hit.getBlockPos(), hit);
        if (bus.post(event)) {
          return event.getCancellationResult() == InteractionResult.PASS ? InteractionResult.FAIL : event.getCancellationResult();
        }
      }
      return InteractionResult.PASS;
    });

    AttackBlockCallback.EVENT.register((player, level, hand, pos, direction) -> {
      if (bus.hasListeners(PlayerInteractEvent.LeftClickBlock.class)) {
        PlayerInteractEvent.LeftClickBlock event = new PlayerInteractEvent.LeftClickBlock(player, pos, direction, PlayerInteractEvent.LeftClickBlock.Action.START);
        if (bus.post(event)) {
          return InteractionResult.FAIL;
        }
      }
      return InteractionResult.PASS;
    });

    UseEntityCallback.EVENT.register((player, level, hand, entity, hit) -> {
      if (bus.hasListeners(PlayerInteractEvent.EntityInteract.class)) {
        PlayerInteractEvent.EntityInteract event = new PlayerInteractEvent.EntityInteract(player, hand, entity);
        if (bus.post(event)) {
          return event.getCancellationResult() == InteractionResult.PASS ? InteractionResult.FAIL : event.getCancellationResult();
        }
      }
      return InteractionResult.PASS;
    });

    AttackEntityCallback.EVENT.register((player, level, hand, entity, hit) -> {
      if (bus.hasListeners(AttackEntityEvent.class) && bus.post(new AttackEntityEvent(player, entity))) {
        return InteractionResult.FAIL;
      }
      return InteractionResult.PASS;
    });
  }
}
