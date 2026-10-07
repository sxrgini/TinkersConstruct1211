package slimeknights.mantle.platform.client.model;

/** Key for a value in {@link ModelData}, identity based. */
public final class ModelProperty<T> {
  public ModelProperty() {}

  /** Constructor with a validity predicate, which is not enforced on Fabric */
  public ModelProperty(java.util.function.Predicate<T> predicate) {}
}
