package slimeknights.mantle.platform.network;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** Handler for a payload */
@FunctionalInterface
public interface IPayloadHandler<T extends CustomPacketPayload> {
  void handle(T payload, IPayloadContext context);
}
