package slimeknights.mantle.platform.fluid;

import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

/** Fluid handler for bucket items holding one bucket of fluid, equivalent of Forge's FluidBucketWrapper */
public class FluidBucketWrapper implements IFluidHandlerItem {
  protected ItemStack container;

  public FluidBucketWrapper(ItemStack container) {
    this.container = container;
  }

  @Override
  public ItemStack getContainer() {
    return container;
  }

  /** Checks if the fluid can be placed in an empty bucket */
  public boolean canFillFluidType(FluidStack fluid) {
    return fluid.getFluid() == Fluids.WATER || fluid.getFluid() == Fluids.LAVA || fluid.getFluid().getBucket() != Items.AIR;
  }

  /** Gets the fluid in the bucket */
  public FluidStack getFluid() {
    if (container.getItem() instanceof BucketItem bucket && bucket.content != Fluids.EMPTY) {
      return new FluidStack(bucket.content, FluidType.BUCKET_VOLUME);
    }
    return FluidStack.EMPTY;
  }

  /** Updates the container for the given fluid */
  protected void setFluid(FluidStack fluid) {
    if (fluid.isEmpty()) {
      container = new ItemStack(Items.BUCKET);
    } else {
      Fluid type = fluid.getFluid();
      container = new ItemStack(type.getBucket());
    }
  }

  @Override
  public int getTanks() {
    return 1;
  }

  @Override
  public FluidStack getFluidInTank(int tank) {
    return getFluid();
  }

  @Override
  public int getTankCapacity(int tank) {
    return FluidType.BUCKET_VOLUME;
  }

  @Override
  public boolean isFluidValid(int tank, FluidStack stack) {
    return canFillFluidType(stack);
  }

  @Override
  public int fill(FluidStack resource, FluidAction action) {
    if (container.getCount() != 1 || resource.getAmount() < FluidType.BUCKET_VOLUME || container.getItem() != Items.BUCKET || !getFluid().isEmpty() || !canFillFluidType(resource)) {
      return 0;
    }
    if (action.execute()) {
      setFluid(resource);
    }
    return FluidType.BUCKET_VOLUME;
  }

  @Override
  public FluidStack drain(FluidStack resource, FluidAction action) {
    if (container.getCount() != 1 || resource.getAmount() < FluidType.BUCKET_VOLUME) {
      return FluidStack.EMPTY;
    }
    FluidStack contained = getFluid();
    if (!contained.isEmpty() && contained.isSameFluid(resource)) {
      if (action.execute()) {
        setFluid(FluidStack.EMPTY);
      }
      return contained;
    }
    return FluidStack.EMPTY;
  }

  @Override
  public FluidStack drain(int maxDrain, FluidAction action) {
    if (container.getCount() != 1 || maxDrain < FluidType.BUCKET_VOLUME) {
      return FluidStack.EMPTY;
    }
    FluidStack contained = getFluid();
    if (!contained.isEmpty()) {
      if (action.execute()) {
        setFluid(FluidStack.EMPTY);
      }
      return contained;
    }
    return FluidStack.EMPTY;
  }
}
