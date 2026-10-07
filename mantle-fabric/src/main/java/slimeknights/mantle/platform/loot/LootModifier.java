package slimeknights.mantle.platform.loot;

import com.mojang.datafixers.Products.P1;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import com.mojang.serialization.codecs.RecordCodecBuilder.Mu;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditions;

/** Base class for loot modifiers with conditions, replacing NeoForge's {@code LootModifier}. */
public abstract class LootModifier implements IGlobalLootModifier {
  protected final LootItemCondition[] conditions;

  protected LootModifier(LootItemCondition[] conditions) {
    this.conditions = conditions;
  }

  /** Starts a codec with the conditions field */
  protected static <T extends LootModifier> P1<Mu<T>,LootItemCondition[]> codecStart(Instance<T> instance) {
    return instance.group(
      LootItemConditions.DIRECT_CODEC.listOf().fieldOf("conditions").xmap(list -> list.toArray(new LootItemCondition[0]), java.util.Arrays::asList).forGetter(m -> m.conditions)
    );
  }

  @Override
  public final ObjectArrayList<ItemStack> apply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
    for (LootItemCondition condition : conditions) {
      if (!condition.test(context)) {
        return generatedLoot;
      }
    }
    return doApply(generatedLoot, context);
  }

  /** Applies the modifier, called only if all conditions pass */
  protected abstract ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context);
}
