package slimeknights.mantle.platform.item;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Ordered registry of tool tiers, replacing Forge's TierSortingRegistry. Vanilla tiers are registered in their standard order,
 * other mods may insert additional tiers relative to existing ones.
 */
public final class TierSortingRegistry {
  private TierSortingRegistry() {}

  private static final List<Tier> SORTED = new ArrayList<>();
  private static final Map<ResourceLocation,Tier> BY_NAME = new HashMap<>();
  private static final Map<Tier,ResourceLocation> NAMES = new HashMap<>();

  static {
    registerTier(Tiers.WOOD, ResourceLocation.withDefaultNamespace("wood"), List.of(), List.of());
    registerTier(Tiers.GOLD, ResourceLocation.withDefaultNamespace("gold"), List.of(Tiers.WOOD), List.of());
    registerTier(Tiers.STONE, ResourceLocation.withDefaultNamespace("stone"), List.of(Tiers.GOLD), List.of());
    registerTier(Tiers.IRON, ResourceLocation.withDefaultNamespace("iron"), List.of(Tiers.STONE), List.of());
    registerTier(Tiers.DIAMOND, ResourceLocation.withDefaultNamespace("diamond"), List.of(Tiers.IRON), List.of());
    registerTier(Tiers.NETHERITE, ResourceLocation.withDefaultNamespace("netherite"), List.of(Tiers.DIAMOND), List.of());
  }

  /**
   * Registers a tier, placing it after the last of the given tiers in the sorted order. If nothing is given to place after, it goes before the first of the before tiers, or at the end.
   */
  public static synchronized void registerTier(Tier tier, ResourceLocation name, List<?> after, List<?> before) {
    if (BY_NAME.containsKey(name)) {
      throw new IllegalStateException("Tier " + name + " is already registered");
    }
    int index = SORTED.size();
    if (!after.isEmpty()) {
      int max = -1;
      for (Object other : after) {
        Tier resolved = other instanceof Tier t ? t : byName(other instanceof ResourceLocation id ? id : ResourceLocation.parse(other.toString()));
        max = Math.max(max, SORTED.indexOf(resolved));
      }
      index = max + 1;
    } else if (!before.isEmpty()) {
      int min = SORTED.size();
      for (Object other : before) {
        Tier resolved = other instanceof Tier t ? t : byName(other instanceof ResourceLocation id ? id : ResourceLocation.parse(other.toString()));
        int found = SORTED.indexOf(resolved);
        if (found >= 0) {
          min = Math.min(min, found);
        }
      }
      index = min;
    }
    SORTED.add(index, tier);
    BY_NAME.put(name, tier);
    NAMES.put(tier, name);
  }

  /** Gets the tiers in order from weakest to strongest */
  public static List<Tier> getSortedTiers() {
    return Collections.unmodifiableList(SORTED);
  }

  /** Gets a tier by its name */
  @Nullable
  public static Tier byName(ResourceLocation name) {
    return BY_NAME.get(name);
  }

  /** Gets the name of a tier */
  @Nullable
  public static ResourceLocation getName(Tier tier) {
    return NAMES.get(tier);
  }

  /** Checks if the tier is high enough to drop items from the given state */
  public static boolean isCorrectTierForDrops(Tier tier, BlockState state) {
    return !state.is(tier.getIncorrectBlocksForDrops());
  }
}
