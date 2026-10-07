package slimeknights.tconstruct.common.network;

import slimeknights.mantle.platform.capability.Caps;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import slimeknights.mantle.platform.capability.Capabilities;
import slimeknights.mantle.platform.item.IItemHandlerModifiable;
import slimeknights.mantle.platform.network.IPayloadContext;
import slimeknights.mantle.network.packet.IThreadsafePacket;

public class InventorySlotSyncPacket implements IThreadsafePacket {

  public final ItemStack itemStack;
  public final int slot;
  public final BlockPos pos;

  public InventorySlotSyncPacket(ItemStack itemStack, int slot, BlockPos pos) {
    this.itemStack = itemStack;
    this.slot = slot;
    this.pos = pos;
  }

  public InventorySlotSyncPacket(RegistryFriendlyByteBuf buffer) {
    this.itemStack = ItemStack.OPTIONAL_STREAM_CODEC.decode(buffer);
    this.slot = buffer.readShort();
    this.pos = buffer.readBlockPos();
  }

  @Override
  public void encode(RegistryFriendlyByteBuf packetBuffer) {
    ItemStack.OPTIONAL_STREAM_CODEC.encode(packetBuffer, this.itemStack);
    packetBuffer.writeShort(this.slot);
    packetBuffer.writeBlockPos(this.pos);
  }

  @Override
  public void handleThreadsafe(IPayloadContext context) {
    HandleClient.handle(this);
  }

  /** Safely runs client side only code in a method only called on client */
  private static class HandleClient {
    private static void handle(InventorySlotSyncPacket packet) {
      Level world = Minecraft.getInstance().level;
      if (world != null) {
        BlockEntity te = world.getBlockEntity(packet.pos);
        if (te != null) {
          Caps.get(te, Capabilities.ITEM_HANDLER)
            .filter(cap -> cap instanceof IItemHandlerModifiable)
            .ifPresent(cap -> {
              ((IItemHandlerModifiable)cap).setStackInSlot(packet.slot, packet.itemStack);
              //noinspection ConstantConditions
              Minecraft.getInstance().levelRenderer.blockChanged(null, packet.pos, null, null, 0);
            });
        }
      }
    }
  }
}
