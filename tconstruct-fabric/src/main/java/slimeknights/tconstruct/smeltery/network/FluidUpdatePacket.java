package slimeknights.tconstruct.smeltery.network;

import lombok.RequiredArgsConstructor;
import lombok.ToString;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import slimeknights.mantle.platform.fluid.FluidStack;
import slimeknights.mantle.platform.network.IPayloadContext;
import slimeknights.mantle.network.packet.IBlockEntityPacket;
import slimeknights.tconstruct.smeltery.network.FluidUpdatePacket.IFluidPacketReceiver;

/**
 * Packet for when the fluid changes in a block entity.
 * TODO 1.21: make record.
 */
@RequiredArgsConstructor
@ToString
public class FluidUpdatePacket implements IBlockEntityPacket<IFluidPacketReceiver> {
  protected final BlockPos pos;
  protected final FluidStack fluid;

  public FluidUpdatePacket(RegistryFriendlyByteBuf buffer) {
    this.pos = buffer.readBlockPos();
    this.fluid = FluidStack.OPTIONAL_STREAM_CODEC.decode(buffer);
  }

  @Override
  public void encode(RegistryFriendlyByteBuf buffer) {
    buffer.writeBlockPos(pos);
    FluidStack.OPTIONAL_STREAM_CODEC.encode(buffer, fluid);
  }

  @Override
  public BlockPos pos() {
    return pos;
  }

  @Override
  public Class<IFluidPacketReceiver> blockEntityType() {
    return IFluidPacketReceiver.class;
  }

  @Override
  public void handleBlockEntity(IPayloadContext context, IFluidPacketReceiver be) {
    be.updateFluidTo(fluid);
  }

  /** Interface to implement for anything wishing to receive fluid updates */
  public interface IFluidPacketReceiver {
    /**
     * Updates the current fluid to the specified value
     * @param fluid New fluidstack
     */
    void updateFluidTo(FluidStack fluid);
  }
}
