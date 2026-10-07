package slimeknights.tconstruct.tables.block.entity.inventory;

import net.minecraft.nbt.CompoundTag;
import slimeknights.mantle.platform.util.INBTSerializable;
import slimeknights.mantle.platform.item.IItemHandlerModifiable;
import slimeknights.mantle.block.entity.MantleBlockEntity;

/** Interface for tinker chest TEs */
public interface IChestItemHandler extends IItemHandlerModifiable, INBTSerializable<CompoundTag>, IScalingContainer {
  /** Sets the parent of this block */
  void setParent(MantleBlockEntity parent);
}
