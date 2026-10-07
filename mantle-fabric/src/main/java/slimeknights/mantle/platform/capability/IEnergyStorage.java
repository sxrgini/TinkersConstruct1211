package slimeknights.mantle.platform.capability;

/** Energy storage interface, equivalent of Forge's IEnergyStorage. Fabric has no standard energy API in vanilla Fabric API. */
public interface IEnergyStorage {
  int receiveEnergy(int maxReceive, boolean simulate);

  int extractEnergy(int maxExtract, boolean simulate);

  int getEnergyStored();

  int getMaxEnergyStored();

  boolean canExtract();

  boolean canReceive();
}
