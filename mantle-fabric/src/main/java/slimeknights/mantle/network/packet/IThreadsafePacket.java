package slimeknights.mantle.network.packet;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import slimeknights.mantle.network.ISimplePacket;
import slimeknights.mantle.network.NetworkWrapper;
import slimeknights.mantle.platform.network.IPayloadContext;

/**
 * Packet with Forge style encode and handle methods, registered through {@link NetworkWrapper}.
 * Fabric already runs handlers on the main thread, so the handler runs directly.
 */
public interface IThreadsafePacket extends ISimplePacket {
  /** Writes the packet to the buffer */
  void encode(RegistryFriendlyByteBuf buf);

  /** Handles receiving the packet on the correct thread */
  void handleThreadsafe(IPayloadContext context);

  @Override
  default void handle(IPayloadContext context) {
    handleThreadsafe(context);
  }

  @Override
  default CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
    return NetworkWrapper.typeOf(getClass());
  }
}
