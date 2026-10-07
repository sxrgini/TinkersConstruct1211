package slimeknights.tconstruct.smeltery.network;

import lombok.AllArgsConstructor;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import slimeknights.mantle.platform.fluid.FluidStack;
import slimeknights.mantle.platform.network.IPayloadContext;
import slimeknights.mantle.network.packet.IBlockEntityPacket;
import slimeknights.tconstruct.smeltery.block.entity.tank.ISmelteryTankHandler;

import java.util.ArrayList;
import java.util.List;

/**
 * Packet sent whenever the contents of the smeltery tank change.
 * TODO 1.21: make record
 */
@AllArgsConstructor
public class SmelteryTankUpdatePacket implements IBlockEntityPacket<ISmelteryTankHandler> {
  private final BlockPos pos;
  private final List<FluidStack> fluids;

  public SmelteryTankUpdatePacket(RegistryFriendlyByteBuf buffer) {
    pos = buffer.readBlockPos();
    int size = buffer.readVarInt();
    fluids = new ArrayList<>(size);
    for (int i = 0; i < size; i++) {
      fluids.add(FluidStack.OPTIONAL_STREAM_CODEC.decode(buffer));
    }
  }

  @Override
  public void encode(RegistryFriendlyByteBuf buffer) {
    buffer.writeBlockPos(pos);
    buffer.writeVarInt(fluids.size());
    for (FluidStack fluid : fluids) {
      FluidStack.OPTIONAL_STREAM_CODEC.encode(buffer, fluid);
    }
  }

  @Override
  public BlockPos pos() {
    return pos;
  }

  @Override
  public Class<ISmelteryTankHandler> blockEntityType() {
    return ISmelteryTankHandler.class;
  }

  @Override
  public void handleBlockEntity(IPayloadContext context, ISmelteryTankHandler be) {
    be.updateFluidsFromPacket(fluids);
  }
}
