package slimeknights.mantle.platform.client.model;

import javax.annotation.Nullable;
import java.util.IdentityHashMap;
import java.util.Map;

/**
 * Extra data passed to dynamic models. On Fabric this is the object returned by {@code RenderDataBlockEntity.getRenderData()}.
 */
public final class ModelData {
  public static final ModelData EMPTY = new ModelData(Map.of());

  private final Map<ModelProperty<?>,Object> properties;

  private ModelData(Map<ModelProperty<?>,Object> properties) {
    this.properties = properties;
  }

  public static Builder builder() {
    return new Builder(new IdentityHashMap<>());
  }

  /** Creates a builder copying this data */
  public Builder derive() {
    return new Builder(new IdentityHashMap<>(properties));
  }

  public boolean has(ModelProperty<?> property) {
    return properties.containsKey(property);
  }

  @Nullable
  @SuppressWarnings("unchecked")
  public <T> T get(ModelProperty<T> property) {
    return (T) properties.get(property);
  }

  /** Builder for model data */
  public static final class Builder {
    private final Map<ModelProperty<?>,Object> properties;

    private Builder(Map<ModelProperty<?>,Object> properties) {
      this.properties = properties;
    }

    public <T> Builder with(ModelProperty<T> property, T value) {
      properties.put(property, value);
      return this;
    }

    public ModelData build() {
      return properties.isEmpty() ? EMPTY : new ModelData(properties);
    }
  }
}
