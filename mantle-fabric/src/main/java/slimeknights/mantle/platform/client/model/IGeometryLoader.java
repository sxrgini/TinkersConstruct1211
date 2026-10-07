package slimeknights.mantle.platform.client.model;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;

/** Reads custom geometry from the model JSON */
@FunctionalInterface
public interface IGeometryLoader<T extends IUnbakedGeometry<T>> {
  T read(JsonObject json, JsonDeserializationContext context);
}
