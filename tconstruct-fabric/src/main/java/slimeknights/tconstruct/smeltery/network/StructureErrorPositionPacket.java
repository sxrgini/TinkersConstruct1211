package slimeknights.tconstruct.smeltery.network;

import lombok.RequiredArgsConstructor;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import slimeknights.mantle.platform.network.IPayloadContext;
import slimeknights.mantle.network.packet.IBlockEntityPacket;
import slimeknights.tconstruct.smeltery.block.entity.controller.HeatingStructureBlockEntity;

import javax.annotation.Nullable;

/**
 * Packet to tell a multiblock to render a specific position as the cause of the error
 */
@RequiredArgsConstructor
public class StructureErrorPositionPacket implements IBlockEntityPacket<HeatingStructureBlockEntity> {
  private final BlockPos controllerPos;
  @Nullable
  private final BlockPos errorPos;

  public StructureErrorPositionPacket(RegistryFriendlyByteBuf buffer) {
    this.controllerPos = buffer.readBlockPos();
    if (buffer.readBoolean()) {
      this.errorPos = buffer.readBlockPos();
    } else {
      this.errorPos = null;
    }
  }

  @Override
  public void encode(RegistryFriendlyByteBuf buffer) {
    buffer.writeBlockPos(controllerPos);
    if (errorPos != null) {
      buffer.writeBoolean(true);
      buffer.writeBlockPos(errorPos);
    } else {
      buffer.writeBoolean(false);
    }
  }

  @Override
  public BlockPos pos() {
    return controllerPos;
  }

  @Override
  public Class<HeatingStructureBlockEntity> blockEntityType() {
    return HeatingStructureBlockEntity.class;
  }

  @Override
  public void handleBlockEntity(IPayloadContext context, HeatingStructureBlockEntity be) {
    be.setErrorPos(errorPos);
  }
}
