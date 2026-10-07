package slimeknights.mantle.platform.capability;

import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.InventoryStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import slimeknights.mantle.platform.fluid.IFluidHandler;
import slimeknights.mantle.platform.fluid.IFluidHandlerItem;

import javax.annotation.Nullable;

/** Replacement for NeoForge's fluid handler capabilities, looking up Fabric Transfer API storages and adapting them. */
public final class FluidHandlers {
  private FluidHandlers() {}

  /** Gets the fluid handler of a block, or null if there is none */
  @Nullable
  public static IFluidHandler getBlock(Level level, BlockPos pos, @Nullable Direction side) {
    Storage<FluidVariant> storage = FluidStorage.SIDED.find(level, pos, side);
    return storage == null ? null : new FluidStorageAdapter(storage);
  }

  /** Gets the fluid handler of an item stack, or null if there is none. Changes apply to the container returned by {@link IFluidHandlerItem#getContainer()}, not the passed stack. */
  @Nullable
  public static IFluidHandlerItem getItem(ItemStack stack) {
    if (stack.isEmpty()) {
      return null;
    }
    SimpleContainer container = new SimpleContainer(stack.copyWithCount(1));
    ContainerItemContext context = ContainerItemContext.ofSingleSlot(InventoryStorage.of(container, null).getSlot(0));
    Storage<FluidVariant> storage = FluidStorage.ITEM.find(container.getItem(0), context);
    return storage == null ? null : new ItemAdapter(storage, container);
  }

  /** Item variant of the adapter, exposing the container stack after modifications */
  private static class ItemAdapter extends FluidStorageAdapter implements IFluidHandlerItem {
    private final SimpleContainer container;

    private ItemAdapter(Storage<FluidVariant> storage, SimpleContainer container) {
      super(storage);
      this.container = container;
    }

    @Override
    public ItemStack getContainer() {
      return container.getItem(0);
    }
  }
}
