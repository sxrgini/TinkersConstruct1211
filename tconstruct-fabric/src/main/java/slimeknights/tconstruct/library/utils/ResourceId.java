package slimeknights.tconstruct.library.utils;

import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;
import java.util.function.UnaryOperator;

/**
 * Helper for use with our wrappers of resource location for some type safety in IDs.
 * {@link ResourceLocation} is final in 1.21, so these wrap a location instead of extending it. Use {@link #location()} where a plain location is needed.
 * Note equality is deliberately lenient: an ID is equal to a plain location with the same namespace and path, and has the same hash code,
 * so IDs can still be used to look up entries in maps keyed by plain locations. The reverse is not true.
 * @see IdParser
 */
public abstract class ResourceId implements Comparable<ResourceId> {
  private final ResourceLocation location;

  public ResourceId(ResourceLocation location) {
    this.location = location;
  }

  public ResourceId(String namespace, String path) {
    this(ResourceLocation.fromNamespaceAndPath(namespace, path));
  }

  public ResourceId(String location) {
    this(ResourceLocation.parse(location));
  }

  /** {@return the wrapped resource location} */
  public ResourceLocation location() {
    return location;
  }

  public String getNamespace() {
    return location.getNamespace();
  }

  public String getPath() {
    return location.getPath();
  }

  /** Same as {@link ResourceLocation#withPath(String)}, returns a plain location */
  public ResourceLocation withPath(String path) {
    return location.withPath(path);
  }

  /** Same as {@link ResourceLocation#withPath(UnaryOperator)}, returns a plain location */
  public ResourceLocation withPath(UnaryOperator<String> path) {
    return location.withPath(path);
  }

  /** Same as {@link ResourceLocation#withPrefix(String)}, returns a plain location */
  public ResourceLocation withPrefix(String prefix) {
    return location.withPrefix(prefix);
  }

  /** Same as {@link ResourceLocation#withSuffix(String)}, returns a plain location */
  public ResourceLocation withSuffix(String suffix) {
    return location.withSuffix(suffix);
  }

  /** {@return Namespace and path joined with an underscore} */
  public String toDebugFileName() {
    return location.toDebugFileName();
  }

  /** {@return Namespace and path joined with a dot} */
  public String toLanguageKey() {
    return location.toLanguageKey();
  }

  @Override
  public int compareTo(ResourceId other) {
    return location.compareTo(other.location);
  }

  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (other instanceof ResourceId id) {
      return location.equals(id.location);
    }
    return other instanceof ResourceLocation loc && location.equals(loc);
  }

  @Override
  public int hashCode() {
    return location.hashCode();
  }

  @Override
  public String toString() {
    return location.toString();
  }


  /* Helpers for static constructors */

  /**
   * Creates a new ID from the given string
   * @param string  String
   * @return  ID, or null if invalid
   */
  @Nullable
  protected static <T extends ResourceId> T tryParse(String string, Function<ResourceLocation,T> constructor) {
    ResourceLocation location = ResourceLocation.tryParse(string);
    return location == null ? null : constructor.apply(location);
  }

  /**
   * Creates a new ID from the given namespace and path
   * @param namespace  Namespace
   * @param path       Path
   * @return  ID, or null if invalid
   */
  @Nullable
  protected static <T extends ResourceId> T tryBuild(String namespace, String path, Function<ResourceLocation,T> constructor) {
    ResourceLocation location = ResourceLocation.tryBuild(namespace, path);
    return location == null ? null : constructor.apply(location);
  }
}
