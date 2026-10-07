package slimeknights.tconstruct.gadgets.block;

import slimeknights.mantle.platform.capability.Caps;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RailBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.Hopper;
import net.minecraft.world.level.block.state.BlockState;
import slimeknights.mantle.platform.capability.Capabilities;
import slimeknights.mantle.platform.item.IItemHandler;
import slimeknights.mantle.platform.item.ItemHandlerHelper;
import slimeknights.mantle.inventory.EmptyItemHandler;

public class DropperRailBlock extends RailBlock {

  public DropperRailBlock(Properties properties) {
    super(properties);
  }

  @Override
  public void onMinecartPass(BlockState state, Level world, BlockPos pos, AbstractMinecart cart) {
    if (!Caps.get(cart, Capabilities.ITEM_HANDLER, Direction.DOWN).isPresent() || !(cart instanceof Hopper)) {
      return;
    }
    BlockEntity tileEntity = world.getBlockEntity(pos.below());
    if (tileEntity == null || !Caps.get(tileEntity, Capabilities.ITEM_HANDLER, Direction.DOWN).isPresent()) {
      return;
    }

    // todo: fix this optional usage
    IItemHandler itemHandlerCart = Caps.get(cart, Capabilities.ITEM_HANDLER, Direction.UP).orElse(EmptyItemHandler.INSTANCE);
    IItemHandler itemHandlerTE = Caps.get(tileEntity, Capabilities.ITEM_HANDLER, Direction.UP).orElse(EmptyItemHandler.INSTANCE);

    for (int i = 0; i < itemHandlerCart.getSlots(); i++) {
      ItemStack itemStack = itemHandlerCart.extractItem(i, 1, true);
      if (itemStack.isEmpty()) {
        continue;
      }
      if (ItemHandlerHelper.insertItem(itemHandlerTE, itemStack, true).isEmpty()) {
        itemStack = itemHandlerCart.extractItem(i, 1, false);
        ItemHandlerHelper.insertItem(itemHandlerTE, itemStack, false);
        break;
      }
    }
  }

}
