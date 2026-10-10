package slimeknights.mantle.data.loadable.primitive;

import com.google.gson.JsonSyntaxException;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.item.ItemDisplayContext;
import slimeknights.mantle.util.typed.TypedMap;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Loadable for item display contexts. The vanilla enum cannot be extended on Fabric, so custom names such as {@code tconstruct:table}
 * are registered with {@link #registerCustom(String, ItemDisplayContext)} and resolve to the vanilla context they fall back to.
 */
public enum ItemDisplayContextLoadable implements StringLoadable<ItemDisplayContext> {
  INSTANCE;

  private static final Map<String,ItemDisplayContext> CUSTOM = new HashMap<>();

  /** Registers a custom context name, resolving to the given vanilla fallback */
  public static ItemDisplayContext registerCustom(String name, ItemDisplayContext fallback) {
    CUSTOM.put(name, fallback);
    return fallback;
  }

  @Override
  public ItemDisplayContext parseString(String name, String key, TypedMap context) {
    ItemDisplayContext custom = CUSTOM.get(name);
    if (custom != null) {
      return custom;
    }
    for (ItemDisplayContext value : ItemDisplayContext.values()) {
      if (value.name().toLowerCase(Locale.ROOT).equals(name)) {
        return value;
      }
    }
    throw new JsonSyntaxException("Invalid ItemDisplayContext " + name);
  }

  @Override
  public String getString(ItemDisplayContext object, TypedMap context) {
    return object.name().toLowerCase(Locale.ROOT);
  }

  @Override
  public ItemDisplayContext decode(RegistryFriendlyByteBuf buffer, TypedMap context) {
    return buffer.readEnum(ItemDisplayContext.class);
  }

  @Override
  public void encode(RegistryFriendlyByteBuf buffer, ItemDisplayContext object, TypedMap context) {
    buffer.writeEnum(object);
  }
}
