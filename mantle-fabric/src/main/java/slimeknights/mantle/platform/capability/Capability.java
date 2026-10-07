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

  public String getName() {
    return name;
  }

  @Override
  public String toString() {
    return "Capability[" + name + "]";
  }
}
