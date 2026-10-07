package slimeknights.mantle.platform.client.model.generators;

import com.google.gson.JsonObject;

/**
 * Minimal replacement for NeoForge's datagen {@code ModelBuilder}. Fabric has no equivalent, so datagen code implements {@link #toJson()}
 * on its own builder and attaches Mantle's custom loaders through {@link CustomLoaderBuilder}.
 */
public abstract class ModelBuilder<T extends ModelBuilder<T>> {
  /** Serializes the model, including any custom loader data */
  public abstract JsonObject toJson();
}
