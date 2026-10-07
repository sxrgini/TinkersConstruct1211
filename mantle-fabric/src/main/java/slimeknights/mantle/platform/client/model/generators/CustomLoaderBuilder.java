package slimeknights.mantle.platform.client.model.generators;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import slimeknights.mantle.platform.data.ExistingFileHelper;

/** Builder for the custom loader portion of a model, replacing NeoForge's {@code CustomLoaderBuilder}. */
public abstract class CustomLoaderBuilder<T extends ModelBuilder<T>> {
  protected final ResourceLocation loaderId;
  protected final T parent;
  protected final ExistingFileHelper existingFileHelper;
  protected final boolean allowInlineElements;

  protected CustomLoaderBuilder(ResourceLocation loaderId, T parent, ExistingFileHelper existingFileHelper, boolean allowInlineElements) {
    this.loaderId = loaderId;
    this.parent = parent;
    this.existingFileHelper = existingFileHelper;
    this.allowInlineElements = allowInlineElements;
  }

  /** Adds the loader data to the model JSON */
  public JsonObject toJson(JsonObject json) {
    json.addProperty("loader", loaderId.toString());
    return json;
  }

  /** Returns to the parent builder */
  public T end() {
    return parent;
  }
}
