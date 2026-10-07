package slimeknights.mantle.platform.network;

import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

/** Sends packets on Fabric, mirroring the NeoForge {@code PacketDistributor} methods Mantle uses. */
public final class PacketDistributor {
  /** Hook set by client initialization to send packets to the server */
  private static ClientSender clientSender = payload -> {
    throw new IllegalStateException("Attempted to send a packet to the server before client initialization");
  };

  private PacketDistributor() {}

  /** Client-side sender hook */
  @FunctionalInterface
  public interface ClientSender {
    void send(CustomPacketPayload payload);
  }

  /** Sets the client sender, called from the client initializer */
  public static void setClientSender(ClientSender sender) {
    clientSender = sender;
  }

  /** Sends a packet from the client to the server */
  public static void sendToServer(CustomPacketPayload payload, CustomPacketPayload... payloads) {
    clientSender.send(payload);
    for (CustomPacketPayload extra : payloads) {
      clientSender.send(extra);
    }
  }

  /** Sends packets to a single player */
  public static void sendToPlayer(ServerPlayer player, CustomPacketPayload payload, CustomPacketPayload... payloads) {
    ServerPlayNetworking.send(player, payload);
    for (CustomPacketPayload extra : payloads) {
      ServerPlayNetworking.send(player, extra);
    }
  }

  /** Sends a packet to all players tracking an entity */
  public static void sendToPlayersTrackingEntity(Entity entity, CustomPacketPayload payload, CustomPacketPayload... payloads) {
    for (ServerPlayer player : PlayerLookup.tracking(entity)) {
      sendToPlayer(player, payload, payloads);
    }
  }

  /** Sends a packet to all players tracking an entity, and the entity itself if it is a player */
  public static void sendToPlayersTrackingEntityAndSelf(Entity entity, CustomPacketPayload payload, CustomPacketPayload... payloads) {
    sendToPlayersTrackingEntity(entity, payload, payloads);
    if (entity instanceof ServerPlayer self) {
      sendToPlayer(self, payload, payloads);
    }
  }
}
