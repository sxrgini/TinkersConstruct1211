package slimeknights.mantle.platform.capability;

import net.minecraft.nbt.Tag;

/** Capability provider that saves data, replacing Forge's {@code ICapabilitySerializable} */
public interface ICapabilitySerializable<T extends Tag> extends ICapabilityProvider {
  T serializeNBT();

  void deserializeNBT(T nbt);
}
