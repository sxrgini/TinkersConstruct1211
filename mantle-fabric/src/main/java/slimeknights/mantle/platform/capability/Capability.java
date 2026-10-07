package slimeknights.mantle.platform.capability;

/**
 * Key for a capability, replacing Forge's {@code Capability}. Fabric has no capability system, so providers are looked up through {@link Caps}.
 * Use identity: create one instance per capability.
 */
public final class Capability<T> {
  private final String name;

  public Capability(String name) {
    this.name = name;
  }

  /** Returns the instance if the requested capability is this one, otherwise empty */
  @SuppressWarnings("unchecked")
  public <R> LazyOptional<R> orEmpty(Capability<R> cap, LazyOptional<T> instance) {
    return cap == this ? instance.cast() : LazyOptional.empty();
  }

  public String getName() {
    return name;
  }

  @Override
  public String toString() {
    return "Capability[" + name + "]";
  }
}
