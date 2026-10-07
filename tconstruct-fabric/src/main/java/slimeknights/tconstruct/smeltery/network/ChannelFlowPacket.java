package slimeknights.tconstruct.smeltery.network;

import lombok.RequiredArgsConstructor;
import lombok.ToString;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import slimeknights.mantle.platform.network.IPayloadContext;
import slimeknights.mantle.network.BlockEntityPacket;
import slimeknights.tconstruct.smeltery.block.entity.ChannelBlockEntity;

/**
 * Packet for when the flowing state changes on a channel side.
 * TODO 1.21: make a record.
 */
@RequiredArgsConstructor
@ToString
public class ChannelFlowPacket implements BlockEntityPacket<ChannelBlockEntity> {
	private final BlockPos pos;
	private final Direction side;
	private final boolean flow;

	public ChannelFlowPacket(RegistryFriendlyByteBuf buffer) {
		pos = buffer.readBlockPos();
		side = buffer.readEnum(Direction.class);
		flow = buffer.readBoolean();
	}

	@Override
	public void encode(RegistryFriendlyByteBuf buffer) {
		buffer.writeBlockPos(pos);
		buffer.writeEnum(side);
		buffer.writeBoolean(flow);
	}

  @Override
  public BlockPos pos() {
    return pos;
  }

  @Override
  public Class<ChannelBlockEntity> type() {
    return ChannelBlockEntity.class;
  }

  @Override
  public void handleBlockEntity(IPayloadContext context, ChannelBlockEntity be) {
    be.setFlow(side, flow);
  }
}
