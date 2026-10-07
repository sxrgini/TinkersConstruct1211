package slimeknights.mantle.loot;

import com.google.gson.JsonDeserializer;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryType;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import slimeknights.mantle.platform.loot.GlobalLootModifierManager;
import slimeknights.mantle.platform.registry.DeferredHolder;
import slimeknights.mantle.Mantle;
import slimeknights.mantle.loot.condition.BlockTagLootCondition;
import slimeknights.mantle.loot.condition.HasLootContextSetCondition;
import slimeknights.mantle.loot.entry.TagPreferenceLootEntry;
import slimeknights.mantle.loot.function.SetFluidLootFunction;
import slimeknights.mantle.loot.modifier.AddEntryLootModifier;
import slimeknights.mantle.loot.modifier.ReplaceItemLootModifier;
import slimeknights.mantle.loot.modifier.condition.ContainsItemModifierLootCondition;
import slimeknights.mantle.loot.modifier.condition.EmptyModifierLootCondition;
import slimeknights.mantle.loot.modifier.condition.ILootModifierCondition;
import slimeknights.mantle.loot.modifier.condition.InvertedModifierLootCondition;
import slimeknights.mantle.recipe.condition.TagEmptyCondition;
import slimeknights.mantle.registration.deferred.LootConditionDeferredRegister;
import slimeknights.mantle.registration.deferred.LootEntryDeferredRegister;
import slimeknights.mantle.registration.deferred.LootFunctionDeferredRegister;

import java.util.Objects;

import static slimeknights.mantle.loot.modifier.condition.ILootModifierCondition.MODIFIER_CONDITIONS;

/** Handles any loot table registration */
public class MantleLoot {
  private static final LootConditionDeferredRegister LOOT_CONDITIONS = new LootConditionDeferredRegister(Mantle.modId);
  private static final LootFunctionDeferredRegister LOOT_FUNCTIONS = new LootFunctionDeferredRegister(Mantle.modId);
  private static final LootEntryDeferredRegister LOOT_ENTRIES = new LootEntryDeferredRegister(Mantle.modId);


  private MantleLoot() {}

  /** Registers this to the bus */
  public static void init() {
    LOOT_CONDITIONS.register();
    LOOT_FUNCTIONS.register();
    LOOT_ENTRIES.register();
  }

  /** Matches if the passed tag is empty */
  public static final DeferredHolder<LootItemConditionType,LootItemConditionType> TAG_EMPTY = LOOT_CONDITIONS.register("tag_empty", TagEmptyCondition.CODEC);
  /** Matches if the passed tag is filled */
  public static final DeferredHolder<LootItemConditionType,LootItemConditionType> TAG_FILLED = LOOT_CONDITIONS.register("tag_filled", slimeknights.mantle.recipe.condition.TagFilledCondition.CODEC);
  /** Condition to match a block tag and property predicate */
  public static final DeferredHolder<LootItemConditionType,LootItemConditionType> BLOCK_TAG_CONDITION = LOOT_CONDITIONS.register("block_tag", BlockTagLootCondition.CODEC);
  /** Condition checking the tool can perform an ability */
  public static final DeferredHolder<LootItemConditionType,LootItemConditionType> CAN_TOOL_PERFORM_ACTION = LOOT_CONDITIONS.register("can_tool_perform_action", slimeknights.mantle.loot.condition.CanToolPerformAction.CODEC);
  /** Condition for global loot modifiers that ensures a context set is present. Useful to check if we are in a specific context like entity. */
  public static final DeferredHolder<LootItemConditionType,LootItemConditionType> HAS_CONTEXT_SET = LOOT_CONDITIONS.register("has_context_set", HasLootContextSetCondition.CODEC);

  /** Function to add block entity texture to a dropped item */
  @SuppressWarnings("removal")
  public static final DeferredHolder<LootItemFunctionType<?>, LootItemFunctionType<slimeknights.mantle.loot.function.RetexturedLootFunction>> RETEXTURED_FUNCTION = LOOT_FUNCTIONS.register("fill_retextured_block", slimeknights.mantle.loot.function.RetexturedLootFunction.CODEC);
  /** Function to add a fluid to an item fluid capability */
  public static final DeferredHolder<LootItemFunctionType<?>, LootItemFunctionType<SetFluidLootFunction>> SET_FLUID_FUNCTION = LOOT_FUNCTIONS.register("set_fluid", SetFluidLootFunction.CODEC);

  /** Entry to pull a value from a tag preference */
  public static final DeferredHolder<LootPoolEntryType, LootPoolEntryType> TAG_PREFERENCE = LOOT_ENTRIES.register("tag_preference", TagPreferenceLootEntry.CODEC);

  /** Registers global loot modifiers and their conditions */
  public static void registerGlobalLootModifiers() {
    GlobalLootModifierManager.register(Mantle.getResource("add_entry"), AddEntryLootModifier.CODEC);
    GlobalLootModifierManager.register(Mantle.getResource("replace_item"), ReplaceItemLootModifier.CODEC);
    GlobalLootModifierManager.init();

    // loot modifier conditions
    MODIFIER_CONDITIONS.registerDeserializer(InvertedModifierLootCondition.ID, (JsonDeserializer<? extends ILootModifierCondition>)InvertedModifierLootCondition::deserialize);
    MODIFIER_CONDITIONS.registerDeserializer(EmptyModifierLootCondition.ID, EmptyModifierLootCondition.INSTANCE);
    MODIFIER_CONDITIONS.registerDeserializer(ContainsItemModifierLootCondition.ID, (JsonDeserializer<? extends ILootModifierCondition>)ContainsItemModifierLootCondition::deserialize);
  }
}
