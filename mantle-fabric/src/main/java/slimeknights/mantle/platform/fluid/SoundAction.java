package slimeknights.mantle.platform.fluid;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/** Named action a fluid can have a sound for, replacing NeoForge's {@code SoundAction}. */
public final class SoundAction {
  private static final ConcurrentMap<String,SoundAction> ACTIONS = new ConcurrentHashMap<>();

  /** Bucket filled with this fluid */
  public static final SoundAction BUCKET_FILL = get("bucket_fill");
  /** Bucket emptied of this fluid */
  public static final SoundAction BUCKET_EMPTY = get("bucket_empty");
  /** Fluid vaporized in the nether */
  public static final SoundAction FLUID_VAPORIZE = get("fluid_vaporize");

  private final String name;

  private SoundAction(String name) {
    this.name = name;
  }

  /** Gets or creates the action with the given name */
  public static SoundAction get(String name) {
    return ACTIONS.computeIfAbsent(name, SoundAction::new);
  }

  public String name() {
    return name;
  }

  @Override
  public String toString() {
    return "SoundAction[" + name + "]";
  }
}
