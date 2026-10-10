package slimeknights.tconstruct.common.data.loot;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.advancements.critereon.NbtPredicate;
import net.minecraft.advancements.critereon.SlimePredicate;
import net.minecraft.data.loot.EntityLootSubProvider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootContext.EntityTarget;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.functions.EnchantedCountIncreaseFunction;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.functions.SmeltItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemEntityPropertyCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.gadgets.TinkerGadgets;
import slimeknights.tconstruct.shared.TinkerCommons;
import slimeknights.tconstruct.shared.block.SlimeType;
import slimeknights.tconstruct.smeltery.TinkerSmeltery;
import slimeknights.tconstruct.world.TinkerWorld;
import slimeknights.tconstruct.world.entity.ArmoredSlimeEntity;

import javax.annotation.Nullable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import net.minecraft.advancements.critereon.EntityFlagsPredicate;
import net.minecraft.resources.ResourceKey;
import java.util.stream.Stream;

public class EntityLootTableProvider extends EntityLootSubProvider {
  protected EntityLootTableProvider(net.minecraft.core.HolderLookup.Provider registries) {
    super(FeatureFlags.REGISTRY.allFlags(), registries);
  }

  /** Tables we generate, vanilla would otherwise demand tables for every registered entity */
  private final Map<ResourceKey<LootTable>,LootTable.Builder> tables = new LinkedHashMap<>();

  @Override
  protected void add(EntityType<?> type, LootTable.Builder builder) {
    add(type, type.getDefaultLootTable(), builder);
  }

  @Override
  protected void add(EntityType<?> type, ResourceKey<LootTable> table, LootTable.Builder builder) {
    tables.put(table, builder);
  }

  @Override
  public void generate(BiConsumer<ResourceKey<LootTable>,LootTable.Builder> consumer) {
    generate();
    tables.forEach(consumer);
  }

  private static final LootItemCondition.Builder ENTITY_ON_FIRE = LootItemEntityPropertyCondition.hasProperties(
    LootContext.EntityTarget.THIS, EntityPredicate.Builder.entity().flags(EntityFlagsPredicate.Builder.flags().setOnFire(true)));

  @Override
  public void generate() {
    this.add(TinkerWorld.skySlimeEntity.get(), dropSlimeballs(SlimeType.SKY, TinkerWorld.steelShard.get()));
    this.add(TinkerWorld.enderSlimeEntity.get(), dropSlimeballs(SlimeType.ENDER, TinkerWorld.knightmetalShard.get()));
    this.add(TinkerWorld.terracubeEntity.get(),
                           LootTable.lootTable().withPool(LootPool.lootPool()
                                                                   .setRolls(ConstantValue.exactly(1))
                                                                   .add(LootItem.lootTableItem(Items.CLAY_BALL)
                                                                                          .apply(SetItemCountFunction.setCount(UniformGenerator.between(-2.0F, 1.0F)))
                                                                                          .apply(EnchantedCountIncreaseFunction.lootingMultiplier(this.registries, UniformGenerator.between(0.0F, 1.0F)))
                                                                                          .apply(SmeltItemFunction.smelted().when(ENTITY_ON_FIRE)))));

    LootItemCondition.Builder killedByFrog = killedByFrog();
    this.add(TinkerWorld.terracubeEntity.get(),
             LootTable.lootTable()
                      .withPool(LootPool.lootPool()
                                        .setRolls(ConstantValue.exactly(1))
                                        .add(LootItem.lootTableItem(Items.CLAY_BALL)
                                                     .apply(SetItemCountFunction.setCount(UniformGenerator.between(-2.0F, 1.0F)))
                                                     .apply(EnchantedCountIncreaseFunction.lootingMultiplier(this.registries, UniformGenerator.between(0.0F, 1.0F)))
                                                     .when(killedByFrog.invert())
                                                     .when(LootItemEntityPropertyCondition.hasProperties(LootContext.EntityTarget.THIS, EntityPredicate.Builder.entity().subPredicate(SlimePredicate.sized(MinMaxBounds.Ints.atLeast(2))))))
                                        .add(LootItem.lootTableItem(TinkerSmeltery.searedLamp)
                                                     .apply(SetItemCountFunction.setCount(ConstantValue.exactly(1.0F)))
                                                     .when(killedByFrog))
                                        .apply(SmeltItemFunction.smelted().when(ENTITY_ON_FIRE))));
    // not really sure why armor stands have a loot table, but vanilla does it
    this.add(TinkerGadgets.armorStandEntity.get(), LootTable.lootTable());
  }

  /** Drops an item using the same chances as slimeballs */
  private LootPoolEntryContainer.Builder<?> slimeball(Item item) {
    return LootItem.lootTableItem(item)
      .apply(SetItemCountFunction.setCount(UniformGenerator.between(0.0F, 2.0F)))
      .apply(EnchantedCountIncreaseFunction.lootingMultiplier(this.registries, UniformGenerator.between(0.0F, 1.0F)));
  }

  /** Drops a frog slimeball */
  private static LootPoolEntryContainer.Builder<?> frogball(Item item) {
    return LootItem.lootTableItem(item).apply(SetItemCountFunction.setCount(ConstantValue.exactly(1)));
  }

  /** Drops slimeballs, and optionally nuggets for metal slimes. */
  private LootTable.Builder dropSlimeballs(SlimeType type, @Nullable Item nugget) {
    LootItemCondition.Builder small = LootItemEntityPropertyCondition.hasProperties(EntityTarget.THIS, EntityPredicate.Builder.entity().subPredicate(SlimePredicate.sized(MinMaxBounds.Ints.exactly(1))));
    LootItemCondition.Builder killedByFrog = killedByFrog();
    LootPool.Builder noFrog = LootPool.lootPool().setRolls(ConstantValue.exactly(1)).when(killedByFrog.invert()).when(small);
    LootPool.Builder frog = LootPool.lootPool().setRolls(ConstantValue.exactly(1)).when(killedByFrog).when(small);
    Item slimeball = TinkerCommons.slimeball.get(type);
    // if given a nugget, add that drop when metal
    if (nugget != null) {
      CompoundTag isMetal = new CompoundTag();
      isMetal.putBoolean(ArmoredSlimeEntity.TAG_METAL, true);
      LootItemCondition.Builder predicate = LootItemEntityPropertyCondition.hasProperties(EntityTarget.THIS, EntityPredicate.Builder.entity().nbt(new NbtPredicate(isMetal)));
      noFrog.add(slimeball(slimeball).when(predicate.invert()));
      noFrog.add(slimeball(nugget).when(predicate));
      frog.add(frogball(slimeball).when(predicate.invert()));
      frog.add(frogball(nugget).when(predicate));
    } else {
      noFrog.add(slimeball(slimeball));
      frog.add(frogball(slimeball));
    }
    return LootTable.lootTable().withPool(noFrog).withPool(frog);
  }
}
