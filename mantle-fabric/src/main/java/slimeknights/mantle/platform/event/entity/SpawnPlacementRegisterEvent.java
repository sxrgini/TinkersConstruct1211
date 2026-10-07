package slimeknights.mantle.platform.event.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.SpawnPlacementType;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.SpawnPlacements.SpawnPredicate;
import net.minecraft.world.level.levelgen.Heightmap;
import org.jetbrains.annotations.Nullable;
import slimeknights.mantle.platform.event.Event;
import slimeknights.mantle.platform.event.lifecycle.IModBusEvent;

/** Registers or modifies spawn placement rules, equivalent of NeoForge's RegisterSpawnPlacementsEvent */
public class SpawnPlacementRegisterEvent extends Event implements IModBusEvent {
  /** How the new predicate combines with an existing one */
  public enum Operation {
    AND, OR, REPLACE
  }

  /** Registers the rules for the entity type. Null placement or heightmap keep the existing values when there are any. */
  @SuppressWarnings({"unchecked", "rawtypes"})
  public <T extends Mob> void register(EntityType<T> type, @Nullable SpawnPlacementType placement, @Nullable Heightmap.Types heightmap, SpawnPredicate<T> predicate, Operation operation) {
    SpawnPlacements.Data existing = SpawnPlacements.DATA_BY_TYPE.get(type);
    if (existing == null) {
      if (placement == null || heightmap == null) {
        throw new IllegalArgumentException("Placement and heightmap are required when registering a new spawn placement for " + type);
      }
      SpawnPlacements.register(type, placement, heightmap, predicate);
      return;
    }
    SpawnPredicate combined = switch (operation) {
      case REPLACE -> predicate;
      case AND -> (SpawnPredicate<T>) (t, level, spawnType, pos, random) -> ((SpawnPredicate) existing.predicate()).test(t, level, spawnType, pos, random) && predicate.test(t, level, spawnType, pos, random);
      case OR -> (SpawnPredicate<T>) (t, level, spawnType, pos, random) -> ((SpawnPredicate) existing.predicate()).test(t, level, spawnType, pos, random) || predicate.test(t, level, spawnType, pos, random);
    };
    SpawnPlacements.DATA_BY_TYPE.put(type, new SpawnPlacements.Data(
      heightmap == null ? existing.heightMap() : heightmap,
      placement == null ? existing.placement() : placement,
      combined));
  }
}
