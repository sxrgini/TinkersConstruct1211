package slimeknights.mantle.data.registry;

import net.minecraft.resources.ResourceLocation;
import slimeknights.mantle.registration.object.IdAwareObject;
import slimeknights.mantle.util.typed.TypedMap;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Generic registry of a {@link IdAwareObject}.
 * @param <T> Type of the component being registered.
 */
public class IdAwareComponentRegistry<T> extends AbstractNamedComponentRegistry<T> {
  /** Registered box expansion types */
  private final Map<ResourceLocation,T> values = new HashMap<>();

  private final java.util.function.Function<T,ResourceLocation> idGetter;

  /** Creates a registry for objects that know their own ID. The type must implement {@link IdAwareObject} */
  public IdAwareComponentRegistry(String errorText) {
    this(errorText, object -> ((IdAwareObject) object).getId());
  }

  /** Creates a registry for objects that have an ID through another means */
  public IdAwareComponentRegistry(String errorText, java.util.function.Function<T,ResourceLocation> idGetter) {
    super(errorText);
    this.idGetter = idGetter;
  }

  /** Registers the value with the given name */
  public synchronized <V extends T> V register(V value) {
    ResourceLocation name = idGetter.apply(value);
    if (values.putIfAbsent(name, value) != null) {
      throw new IllegalArgumentException("Duplicate registration " + name);
    }
    return value;
  }

  /** Gets a value or null if missing */
  @Override
  @Nullable
  public T getValue(ResourceLocation name) {
    return values.get(name);
  }

  @Override
  public ResourceLocation getKey(T object, TypedMap context) {
    return idGetter.apply(object);
  }

  @Override
  public Collection<ResourceLocation> getKeys() {
    return values.keySet();
  }

  @Override
  public Collection<T> getValues() {
    return values.values();
  }
}
