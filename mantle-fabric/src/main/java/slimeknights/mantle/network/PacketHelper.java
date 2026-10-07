package slimeknights.mantle.network;

import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import slimeknights.mantle.platform.network.PacketDistributor;

/** Helper methods used alongside {@link slimeknights.mantle.platform.network.PacketDistributor} to send packets */
public class PacketHelper {
  /**
   * Sends a vanilla packet to the given entity
   * @param player  Player receiving the packet
   * @param packet  Packet
   */
  public static void sendVanillaPacket(Packet<?> packet, Entity player) {
    if (player instanceof ServerPlayer sPlayer) {
      sPlayer.connection.send(packet);
    }
  }

  /** Sends a packet to a player, handling the server player check */
  public static void sendToPlayer(Player player, CustomPacketPayload payload, CustomPacketPayload... payloads) {
    if (player instanceof ServerPlayer sPlayer) {
      PacketDistributor.sendToPlayer(sPlayer, payload, payloads);
    }
  }


  /** Sends packets for a static registry, which is skipped on an integrated server */
  public static void sendStaticRegistry(ServerPlayer player, CustomPacketPayload payload, CustomPacketPayload... payloads) {
    if (!player.connection.connection.isMemoryConnection()) {
      PacketDistributor.sendToPlayer(player, payload, payloads);
    }
  }

}
