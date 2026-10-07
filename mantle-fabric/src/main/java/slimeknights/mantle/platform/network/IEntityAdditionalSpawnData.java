package slimeknights.mantle.platform.network;

import net.minecraft.network.RegistryFriendlyByteBuf;

/**
 * Entity with extra data synced to the client when it starts being tracked, equivalent of Forge's IEntityAdditionalSpawnData.
 * Fabric has no hook in the spawn packet, so the data arrives in a follow up packet right after the entity is added on the client.
 */
public interface IEntityAdditionalSpawnData {
  /** Writes the data on the server */
  void writeSpawnData(RegistryFriendlyByteBuf buffer);

  /** Reads the data on the client */
  void readSpawnData(RegistryFriendlyByteBuf buffer);
}
