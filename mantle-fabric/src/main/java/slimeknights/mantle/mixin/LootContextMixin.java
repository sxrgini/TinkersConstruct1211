package slimeknights.mantle.mixin;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import slimeknights.mantle.platform.loot.QueriedLootTable;

/** Adds the queried loot table ID to the loot context */
@Mixin(LootContext.class)
public class LootContextMixin implements QueriedLootTable {
  @Unique
  private ResourceLocation mantle$queriedLootTableId;

  @Override
  public ResourceLocation mantle$getQueriedLootTableId() {
    return mantle$queriedLootTableId;
  }

  @Override
  public void mantle$setQueriedLootTableId(ResourceLocation id) {
    this.mantle$queriedLootTableId = id;
  }
}
