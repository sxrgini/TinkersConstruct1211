package slimeknights.mantle.loot.injection;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.errorprone.annotations.CheckReturnValue;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import slimeknights.mantle.Mantle;
import slimeknights.mantle.data.loadable.Loadables;
import slimeknights.mantle.data.loadable.primitive.StringLoadable;
import slimeknights.mantle.data.loadable.record.RecordLoadable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Record holding a list of entries to inject into the given loot table
 */
public record LootTableInjection(ResourceLocation name, List<LootPoolInjection> pools) {
  public static final RecordLoadable<LootTableInjection> LOADABLE = RecordLoadable.create(
    Loadables.RESOURCE_LOCATION.requiredField("name", LootTableInjection::name),
    LootPoolInjection.LOADABLE.list(1).requiredField("pools", LootTableInjection::pools),
    LootTableInjection::new);

  /**
   * Record holding a list of entries to inject into the given pool
   */
  public record LootPoolInjection(String name, List<LootPoolEntryContainer> entries) {
    public static final RecordLoadable<LootPoolInjection> LOADABLE = RecordLoadable.create(
      StringLoadable.DEFAULT.requiredField("name", LootPoolInjection::name),
      Loadables.LOOT_ENTRY.list(1).requiredField("entries", pool -> pool.entries),
      LootPoolInjection::new);

    /** Finds a pool by name. Vanilla pools have no names, so pools are matched by index using the names {@code pool0}, {@code pool1}, ... and {@code main} for the first pool. */
    @javax.annotation.Nullable
    private static LootPool findPool(LootTable table, String name) {
      int index = -1;
      if (name.equals("main")) {
        index = 0;
      } else if (name.matches("pool\\d+")) {
        index = Integer.parseInt(name.substring(4));
      }
      return index >= 0 && index < table.pools.size() ? table.pools.get(index) : null;
    }

    /** Injects this into the given loot pool */
    public void inject(LootTable table) {
      LootPool pool = findPool(table, name);
      //noinspection ConstantConditions method is annotated wrongly
      if (pool != null) {
        List<LootPoolEntryContainer> entries = new ArrayList<>(pool.entries.size() + this.entries.size());
        entries.addAll(pool.entries);
        entries.addAll(this.entries);
        pool.entries = List.copyOf(entries);
      } else {
        Mantle.logger.warn("Failed to inject loot into pool {}", name);
      }
    }
  }

  /** Builder instance for a loot table injection */
  @SuppressWarnings("unused") // API
  @CanIgnoreReturnValue
  public static class Builder {
    private final Map<String,List<LootPoolEntryContainer>> pools = new LinkedHashMap<>();

    /** Inserts the given entries into the pool */
    public Builder addToPool(String name, List<LootPoolEntryContainer> entries) {
      pools.computeIfAbsent(name, n -> new ArrayList<>()).addAll(entries);
      return this;
    }

    /** Inserts the given entries into the pool */
    public Builder addToPool(String name, LootPoolEntryContainer... entries) {
      return addToPool(name, List.of(entries));
    }

    /** Inserts the given entries into the pool */
    public Builder addToPool(LootPoolInjection injection) {
      return addToPool(injection.name, injection.entries);
    }

    /** Builds the list of injections */
    @CheckReturnValue
    public LootTableInjection build(ResourceLocation name) {
      return new LootTableInjection(name, pools.entrySet().stream().map(entry -> new LootPoolInjection(entry.getKey(), List.copyOf(entry.getValue()))).toList());
    }
  }
}
