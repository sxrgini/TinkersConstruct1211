package slimeknights.mantle.platform.util;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.Tag;

/** Object that saves to and loads from NBT, equivalent of NeoForge's INBTSerializable */
public interface INBTSerializable<T extends Tag> {
  /** Saves the object to NBT */
  T serializeNBT(HolderLookup.Provider provider);

  /** Loads the object from NBT */
  void deserializeNBT(HolderLookup.Provider provider, T nbt);

  /** Saves the object using the global registry access, for code without access to a level */
  default T serializeNBT() {
    return serializeNBT(slimeknights.mantle.util.GlobalRegistries.get());
  }

  /** Loads the object using the global registry access, for code without access to a level */
  default void deserializeNBT(T nbt) {
    deserializeNBT(slimeknights.mantle.util.GlobalRegistries.get(), nbt);
  }
}
