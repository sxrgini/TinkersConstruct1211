package slimeknights.mantle.platform.capability;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.core.component.DataComponentPatch;
import slimeknights.mantle.platform.fluid.FluidStack;
import slimeknights.mantle.platform.fluid.IFluidHandler;

import java.util.ArrayList;
import java.util.List;

/** Exposes a Fabric Transfer API fluid storage as an {@link IFluidHandler}. Amounts convert between mB and droplets (81 per mB). */
public class FluidStorageAdapter implements IFluidHandler {
  /** Droplets per millibucket */
  public static final long DROPLETS_PER_MB = FluidConstants.BUCKET / 1000;

  protected final Storage<FluidVariant> storage;

  public FluidStorageAdapter(Storage<FluidVariant> storage) {
    this.storage = storage;
  }

  /** Converts a stack into a Fabric variant */
  public static FluidVariant toVariant(FluidStack stack) {
    return FluidVariant.of(stack.getFluid(), stack.getComponentsPatch());
  }

  /** Converts a variant and droplet amount into a stack */
  public static FluidStack toStack(FluidVariant variant, long droplets) {
    if (variant.isBlank() || droplets <= 0) {
      return FluidStack.EMPTY;
    }
    return new FluidStack(variant.getFluid(), (int) Math.min(Integer.MAX_VALUE, droplets / DROPLETS_PER_MB), variant.getComponents());
  }

  private List<StorageView<FluidVariant>> views() {
    List<StorageView<FluidVariant>> list = new ArrayList<>();
    try (Transaction tx = Transaction.openOuter()) {
      for (StorageView<FluidVariant> view : storage) {
        list.add(view);
      }
    }
    return list;
  }

  @Override
  public int getTanks() {
    return views().size();
  }

  @Override
  public FluidStack getFluidInTank(int tank) {
    List<StorageView<FluidVariant>> views = views();
    if (tank < 0 || tank >= views.size()) {
      return FluidStack.EMPTY;
    }
    StorageView<FluidVariant> view = views.get(tank);
    return toStack(view.getResource(), view.getAmount());
  }

  @Override
  public int getTankCapacity(int tank) {
    List<StorageView<FluidVariant>> views = views();
    if (tank < 0 || tank >= views.size()) {
      return 0;
    }
    return (int) Math.min(Integer.MAX_VALUE, views.get(tank).getCapacity() / DROPLETS_PER_MB);
  }

  @Override
  public boolean isFluidValid(int tank, FluidStack stack) {
    if (stack.isEmpty()) {
      return false;
    }
    try (Transaction tx = Transaction.openOuter()) {
      return net.fabricmc.fabric.api.transfer.v1.storage.StorageUtil.simulateInsert(storage, toVariant(stack), DROPLETS_PER_MB, tx) > 0;
    }
  }

  @Override
  public int fill(FluidStack resource, FluidAction action) {
    if (resource.isEmpty()) {
      return 0;
    }
    try (Transaction tx = Transaction.openOuter()) {
      long inserted = storage.insert(toVariant(resource), resource.getAmount() * DROPLETS_PER_MB, tx);
      if (action.execute()) {
        tx.commit();
      }
      return (int) (inserted / DROPLETS_PER_MB);
    }
  }

  @Override
  public FluidStack drain(FluidStack resource, FluidAction action) {
    if (resource.isEmpty()) {
      return FluidStack.EMPTY;
    }
    try (Transaction tx = Transaction.openOuter()) {
      long extracted = storage.extract(toVariant(resource), resource.getAmount() * DROPLETS_PER_MB, tx);
      if (action.execute()) {
        tx.commit();
      }
      return extracted <= 0 ? FluidStack.EMPTY : resource.copyWithAmount((int) (extracted / DROPLETS_PER_MB));
    }
  }

  @Override
  public FluidStack drain(int maxDrain, FluidAction action) {
    if (maxDrain <= 0) {
      return FluidStack.EMPTY;
    }
    try (Transaction tx = Transaction.openOuter()) {
      for (StorageView<FluidVariant> view : storage) {
        if (view.isResourceBlank()) {
          continue;
        }
        FluidVariant variant = view.getResource();
        long extracted = storage.extract(variant, maxDrain * DROPLETS_PER_MB, tx);
        if (extracted > 0) {
          if (action.execute()) {
            tx.commit();
          }
          return toStack(variant, extracted);
        }
      }
    }
    return FluidStack.EMPTY;
  }
}
