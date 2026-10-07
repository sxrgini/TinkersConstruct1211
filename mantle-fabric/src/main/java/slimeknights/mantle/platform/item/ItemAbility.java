package slimeknights.mantle.platform.item;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/** Named ability an item can perform, replacing NeoForge's {@code ItemAbility}. Items advertise abilities by implementing {@link ItemAbilityProvider}. */
public final class ItemAbility {
  private static final ConcurrentMap<String,ItemAbility> ABILITIES = new ConcurrentHashMap<>();
  private final String name;

  private ItemAbility(String name) {
    this.name = name;
  }

  /** Gets or creates the ability with the given name */
  public static ItemAbility get(String name) {
    return ABILITIES.computeIfAbsent(name, ItemAbility::new);
  }

  public String name() {
    return name;
  }

  @Override
  public String toString() {
    return "ItemAbility[" + name + "]";
  }
}
