package slimeknights.mantle.loot.condition;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import slimeknights.mantle.loot.MantleLoot;

/** Loot condition checking the ID of the loot table being generated, replacing NeoForge's condition of the same name */
public record LootTableIdCondition(ResourceLocation id) implements LootItemCondition {
  public static final MapCodec<LootTableIdCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
    ResourceLocation.CODEC.fieldOf("loot_table_id").forGetter(LootTableIdCondition::id)
  ).apply(instance, LootTableIdCondition::new));

  /** Creates a builder for the given loot table */
  public static LootItemCondition.Builder builder(ResourceLocation id) {
    return () -> new LootTableIdCondition(id);
  }

  @Override
  public boolean test(LootContext context) {
    return id.equals(slimeknights.mantle.platform.loot.QueriedLootTable.get(context));
  }

  @Override
  public LootItemConditionType getType() {
    return MantleLoot.LOOT_TABLE_ID.get();
  }
}
