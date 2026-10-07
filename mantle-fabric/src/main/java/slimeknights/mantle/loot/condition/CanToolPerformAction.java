package slimeknights.mantle.loot.condition;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import com.mojang.serialization.Codec;
import slimeknights.mantle.loot.MantleLoot;
import slimeknights.mantle.platform.item.ItemAbilities;
import slimeknights.mantle.platform.item.ItemAbility;

import java.util.Set;

/** Loot condition checking that the tool used can perform the given ability, replacing NeoForge's condition of the same name */
public record CanToolPerformAction(ItemAbility ability) implements LootItemCondition {
  public static final MapCodec<CanToolPerformAction> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
    Codec.STRING.xmap(ItemAbility::get, ItemAbility::name).fieldOf("action").forGetter(CanToolPerformAction::ability)
  ).apply(instance, CanToolPerformAction::new));

  /** Creates a builder for the given ability */
  public static LootItemCondition.Builder canToolPerformAction(ItemAbility ability) {
    return () -> new CanToolPerformAction(ability);
  }

  @Override
  public boolean test(LootContext context) {
    ItemStack stack = context.getParamOrNull(LootContextParams.TOOL);
    return stack != null && ItemAbilities.canPerform(stack, ability);
  }

  @Override
  public Set<LootContextParam<?>> getReferencedContextParams() {
    return Set.of(LootContextParams.TOOL);
  }

  @Override
  public LootItemConditionType getType() {
    return MantleLoot.CAN_TOOL_PERFORM_ACTION.get();
  }
}
