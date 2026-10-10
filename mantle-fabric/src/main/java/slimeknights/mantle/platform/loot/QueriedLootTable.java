package slimeknights.mantle.platform.loot;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import org.jetbrains.annotations.Nullable;

import java.util.IdentityHashMap;
import java.util.Map;

/**
 * Duck interface on {@link LootContext} tracking the ID of the loot table being rolled, vanilla does not store this but NeoForge adds it.
 * Set by {@link slimeknights.mantle.mixin.LootTableMixin}.
 */
public interface QueriedLootTable {
  /** Gets the ID of the loot table being rolled, null if unknown */
  @Nullable
  ResourceLocation mantle$getQueriedLootTableId();

  /** Sets the ID of the loot table being rolled */
  void mantle$setQueriedLootTableId(@Nullable ResourceLocation id);

  /** Gets the ID of the loot table being rolled */
  @Nullable
  static ResourceLocation get(LootContext context) {
    return ((QueriedLootTable) context).mantle$getQueriedLootTableId();
  }

  /** Cache of loot table IDs, rebuilt when the loot table registry instance changes */
  class Cache {
    private static Object source;
    private static Map<LootTable,ResourceLocation> ids = new IdentityHashMap<>();

    private Cache() {}

    /** Finds the ID of the given loot table in the context's server */
    @Nullable
    public static synchronized ResourceLocation find(LootTable table, LootContext context) {
      var server = context.getLevel().getServer();
      if (server == null) {
        return null;
      }
      var registry = server.reloadableRegistries().get().lookupOrThrow(net.minecraft.core.registries.Registries.LOOT_TABLE);
      if (registry != source) {
        source = registry;
        ids = new IdentityHashMap<>();
        registry.listElements().forEach(holder -> ids.put(holder.value(), holder.key().location()));
      }
      return ids.get(table);
    }
  }
}
