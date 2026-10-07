package slimeknights.mantle.platform.util;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.Tag;

/** Object that saves to and loads from NBT, equivalent of NeoForge's INBTSerializable */
public interface INBTSerializable<T extends Tag> {
  /** Saves the object to NBT */
  T serializeNBT(HolderLookup.Provider provider);

  /** Loads the object from NBT */
  void deserializeNBT(HolderLookup.Provider provider, T nbt);
}
