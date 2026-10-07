package slimeknights.tconstruct.smeltery.network;

import lombok.RequiredArgsConstructor;
import lombok.ToString;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import slimeknights.mantle.platform.fluid.FluidStack;
import slimeknights.mantle.platform.network.IPayloadContext;
import slimeknights.mantle.network.packet.IBlockEntityPacket;
import slimeknights.tconstruct.smeltery.block.entity.FaucetBlockEntity;

/**
 * Sent to clients to activate the faucet animation clientside.
 * TODO 1.21: make record.
 */
@RequiredArgsConstructor
@ToString(callSuper = true)
public class FaucetActivationPacket implements IBlockEntityPacket<FaucetBlockEntity> {
  protected final BlockPos pos;
  protected final FluidStack fluid;
  private final boolean isPouring;

  public FaucetActivationPacket(RegistryFriendlyByteBuf buffer) {
    this.pos = buffer.readBlockPos();
    this.fluid = FluidStack.OPTIONAL_STREAM_CODEC.decode(buffer);
    this.isPouring = buffer.readBoolean();
  }

  @Override
  public void encode(RegistryFriendlyByteBuf buffer) {
    buffer.writeBlockPos(pos);
    FluidStack.OPTIONAL_STREAM_CODEC.encode(buffer, fluid);
    buffer.writeBoolean(isPouring);
  }

  @Override
  public BlockPos pos() {
    return pos;
  }

  @Override
  public Class<FaucetBlockEntity> blockEntityType() {
    return FaucetBlockEntity.class;
  }

  @Override
  public void handleBlockEntity(IPayloadContext context, FaucetBlockEntity be) {
    be.onActivationPacket(fluid, isPouring);
  }
}
