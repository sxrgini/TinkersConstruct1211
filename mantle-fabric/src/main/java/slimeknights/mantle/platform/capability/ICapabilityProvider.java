package slimeknights.mantle.platform.capability;

import net.minecraft.core.Direction;

import javax.annotation.Nullable;

/** Something that can provide capabilities, replacing Forge's {@code ICapabilityProvider} */
public interface ICapabilityProvider {
  /** Gets the capability for the given side, empty if not provided */
  <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side);

  /** Gets the capability with no side */
  default <T> LazyOptional<T> getCapability(Capability<T> cap) {
    return getCapability(cap, null);
  }
}
