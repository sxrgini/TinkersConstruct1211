package slimeknights.mantle.network.packet;

import slimeknights.mantle.network.BlockEntityPacket;
import slimeknights.mantle.platform.network.IPayloadContext;

/** Block entity packet registered through {@link slimeknights.mantle.network.NetworkWrapper} with the Forge style encode and buffer constructor */
public interface IBlockEntityPacket<T> extends BlockEntityPacket<T>, IThreadsafePacket {
  @Override
  default void handleThreadsafe(IPayloadContext context) {
    handle(context);
  }

  @Override
  default void handle(IPayloadContext context) {
    BlockEntityPacket.super.handle(context);
  }
}
