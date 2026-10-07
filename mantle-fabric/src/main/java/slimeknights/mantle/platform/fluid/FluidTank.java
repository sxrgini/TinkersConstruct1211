package slimeknights.mantle.platform.fluid;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;

import java.util.function.Predicate;

/** Simple single fluid tank, replacing Forge's {@code FluidTank} */
public class FluidTank implements IFluidHandler, IFluidTank {
  protected Predicate<FluidStack> validator;
  protected FluidStack fluid = FluidStack.EMPTY;
  protected int capacity;

  public FluidTank(int capacity) {
    this(capacity, e -> true);
  }

  public FluidTank(int capacity, Predicate<FluidStack> validator) {
    this.capacity = capacity;
    this.validator = validator;
  }

  public FluidTank setCapacity(int capacity) {
    this.capacity = capacity;
    return this;
  }

  public FluidTank setValidator(Predicate<FluidStack> validator) {
    if (validator != null) {
      this.validator = validator;
    }
    return this;
  }

  @Override
  public boolean isFluidValid(FluidStack stack) {
    return validator.test(stack);
  }

  @Override
  public int getCapacity() {
    return capacity;
  }

  @Override
  public FluidStack getFluid() {
    return fluid;
  }

  @Override
  public int getFluidAmount() {
    return fluid.getAmount();
  }

  public void setFluid(FluidStack stack) {
    this.fluid = stack;
  }

  public boolean isEmpty() {
    return fluid.isEmpty();
  }

  public int getSpace() {
    return Math.max(0, capacity - fluid.getAmount());
  }

  /** Reads the tank from NBT using the registry access to decode components */
  public FluidTank readFromNBT(HolderLookup.Provider registries, CompoundTag nbt) {
    if (!nbt.contains("Fluid")) {
      fluid = FluidStack.EMPTY;
    } else {
      fluid = FluidStack.OPTIONAL_CODEC.parse(registries.createSerializationContext(NbtOps.INSTANCE), nbt.get("Fluid")).result().orElse(FluidStack.EMPTY);
    }
    return this;
  }

  /** Writes the tank to NBT */
  public CompoundTag writeToNBT(HolderLookup.Provider registries, CompoundTag nbt) {
    if (!fluid.isEmpty()) {
      Tag tag = FluidStack.OPTIONAL_CODEC.encodeStart(registries.createSerializationContext(NbtOps.INSTANCE), fluid).result().orElse(null);
      if (tag != null) {
        nbt.put("Fluid", tag);
      }
    }
    return nbt;
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
    return getCapacity();
  }

  @Override
  public boolean isFluidValid(int tank, FluidStack stack) {
    return isFluidValid(stack);
  }

  @Override
  public int fill(FluidStack resource, FluidAction action) {
    if (resource.isEmpty() || !isFluidValid(resource)) {
      return 0;
    }
    if (action.simulate()) {
      if (fluid.isEmpty()) {
        return Math.min(capacity, resource.getAmount());
      }
      if (!fluid.isSameFluidSameComponents(resource)) {
        return 0;
      }
      return Math.min(capacity - fluid.getAmount(), resource.getAmount());
    }
    if (fluid.isEmpty()) {
      fluid = resource.copyWithAmount(Math.min(capacity, resource.getAmount()));
      onContentsChanged();
      return fluid.getAmount();
    }
    if (!fluid.isSameFluidSameComponents(resource)) {
      return 0;
    }
    int filled = capacity - fluid.getAmount();
    if (resource.getAmount() < filled) {
      fluid.grow(resource.getAmount());
      filled = resource.getAmount();
    } else {
      fluid.setAmount(capacity);
    }
    if (filled > 0) {
      onContentsChanged();
    }
    return filled;
  }

  @Override
  public FluidStack drain(FluidStack resource, FluidAction action) {
    if (resource.isEmpty() || !resource.isSameFluidSameComponents(fluid)) {
      return FluidStack.EMPTY;
    }
    return drain(resource.getAmount(), action);
  }

  @Override
  public FluidStack drain(int maxDrain, FluidAction action) {
    int drained = Math.min(fluid.getAmount(), maxDrain);
    FluidStack stack = fluid.copyWithAmount(drained);
    if (action.execute() && drained > 0) {
      fluid.shrink(drained);
      onContentsChanged();
    }
    return stack;
  }

  protected void onContentsChanged() {}
}
