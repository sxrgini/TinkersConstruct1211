package slimeknights.mantle.platform.loot;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;

/** Modifier applied to the output of every loot table, replacing NeoForge's {@code IGlobalLootModifier}. */
public interface IGlobalLootModifier {
  /** Dispatch codec using the registered modifier types */
  Codec<IGlobalLootModifier> DIRECT_CODEC = GlobalLootModifierManager.TYPE_CODEC.dispatch(IGlobalLootModifier::codec, c -> c);

  /** Applies the modifier to the generated loot, returning the new list */
  ObjectArrayList<ItemStack> apply(ObjectArrayList<ItemStack> generatedLoot, LootContext context);

  /** Gets the codec used to serialize this modifier */
  MapCodec<? extends IGlobalLootModifier> codec();
}
