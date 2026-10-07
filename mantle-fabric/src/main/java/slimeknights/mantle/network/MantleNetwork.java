package slimeknights.mantle.network;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jetbrains.annotations.ApiStatus.Internal;
import slimeknights.mantle.fluid.transfer.FluidContainerTransferPacket;
import slimeknights.mantle.network.packet.DropLecternBookPacket;
import slimeknights.mantle.network.packet.OpenLecternBookPacket;
import slimeknights.mantle.network.packet.SwingArmPacket;
import slimeknights.mantle.network.packet.UpdateHeldPagePacket;
import slimeknights.mantle.network.packet.UpdateInventoryPagePacket;
import slimeknights.mantle.network.packet.UpdateLecternPagePacket;

/** Handles registering all packets used by Mantle */
public class MantleNetwork {
  /** Registers packet types and server handlers, call from the common initializer */
  @Internal
  public static void registerPackets() {
    // to server
    toServer(UpdateHeldPagePacket.TYPE, UpdateHeldPagePacket.CODEC);
    toServer(UpdateInventoryPagePacket.TYPE, UpdateInventoryPagePacket.CODEC);
    toServer(UpdateLecternPagePacket.TYPE, UpdateLecternPagePacket.CODEC);
    toServer(DropLecternBookPacket.TYPE, DropLecternBookPacket.CODEC);

    // to client, types only, handlers are registered in registerClientHandlers
    PayloadTypeRegistry.playS2C().register(OpenLecternBookPacket.TYPE, OpenLecternBookPacket.CODEC);
    PayloadTypeRegistry.playS2C().register(SwingArmPacket.TYPE, SwingArmPacket.CODEC);
    PayloadTypeRegistry.playS2C().register(FluidContainerTransferPacket.TYPE, FluidContainerTransferPacket.CODEC);
    PayloadTypeRegistry.playS2C().register(slimeknights.mantle.network.packet.AdditionalSpawnDataPacket.TYPE, slimeknights.mantle.network.packet.AdditionalSpawnDataPacket.CODEC);
    slimeknights.mantle.network.packet.AdditionalSpawnDataPacket.init();
  }

  /** Registers client packet handlers, call from the client initializer */
  @Internal
  @Environment(EnvType.CLIENT)
  public static void registerClientHandlers() {
    toClient(OpenLecternBookPacket.TYPE);
    toClient(SwingArmPacket.TYPE);
    toClient(FluidContainerTransferPacket.TYPE);
    toClient(slimeknights.mantle.network.packet.AdditionalSpawnDataPacket.TYPE);
  }

  private static <T extends ISimplePacket> void toServer(CustomPacketPayload.Type<T> type, StreamCodec<? super RegistryFriendlyByteBuf,T> codec) {
    PayloadTypeRegistry.playC2S().register(type, codec);
    ServerPlayNetworking.registerGlobalReceiver(type, (payload, context) -> payload.handle(context::player));
  }

  @Environment(EnvType.CLIENT)
  private static <T extends ISimplePacket> void toClient(CustomPacketPayload.Type<T> type) {
    ClientPlayNetworking.registerGlobalReceiver(type, (payload, context) -> payload.handle(context::player));
  }
}
