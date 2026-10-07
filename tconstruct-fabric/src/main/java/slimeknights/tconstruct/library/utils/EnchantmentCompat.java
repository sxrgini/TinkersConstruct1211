package slimeknights.tconstruct.library.utils;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import slimeknights.mantle.util.GlobalRegistries;

import javax.annotation.Nullable;

/** Helpers for the data driven enchantments of 1.21, where enchantments are keys that must be looked up in the registry. */
public final class EnchantmentCompat {
  private EnchantmentCompat() {}

  /** Gets the holder for the given enchantment key from the given registries, or null if missing */
  @Nullable
  public static Holder<Enchantment> holder(HolderLookup.Provider registries, ResourceKey<Enchantment> key) {
    return registries.lookupOrThrow(Registries.ENCHANTMENT).get(key).orElse(null);
  }

  /** Gets the holder for the given enchantment key using the global registries, or null if missing */
  @Nullable
  public static Holder<Enchantment> holder(ResourceKey<Enchantment> key) {
    return holder(GlobalRegistries.get(), key);
  }

  /** Gets the level of the given enchantment on a stack, or 0 if missing */
  public static int level(ItemStack stack, ResourceKey<Enchantment> key) {
    Holder<Enchantment> holder = holder(key);
    return holder == null ? 0 : EnchantmentHelper.getItemEnchantmentLevel(holder, stack);
  }

  /** Converts the enchantments on a stack into a mutable key map */
  public static java.util.Map<ResourceKey<Enchantment>,Integer> toMap(net.minecraft.world.item.enchantment.ItemEnchantments enchantments) {
    java.util.Map<ResourceKey<Enchantment>,Integer> map = new java.util.HashMap<>();
    for (it.unimi.dsi.fastutil.objects.Object2IntMap.Entry<Holder<Enchantment>> entry : enchantments.entrySet()) {
      entry.getKey().unwrapKey().ifPresent(key -> map.put(key, entry.getIntValue()));
    }
    return map;
  }

  /** Converts a key map to item enchantments, skipping missing enchantments */
  public static net.minecraft.world.item.enchantment.ItemEnchantments toEnchantments(java.util.Map<ResourceKey<Enchantment>,Integer> map) {
    net.minecraft.world.item.enchantment.ItemEnchantments.Mutable mutable = new net.minecraft.world.item.enchantment.ItemEnchantments.Mutable(net.minecraft.world.item.enchantment.ItemEnchantments.EMPTY);
    for (java.util.Map.Entry<ResourceKey<Enchantment>,Integer> entry : map.entrySet()) {
      Holder<Enchantment> holder = holder(entry.getKey());
      if (holder != null && entry.getValue() > 0) {
        mutable.set(holder, entry.getValue());
      }
    }
    return mutable.toImmutable();
  }

  /** Translation key of an enchantment */
  public static net.minecraft.network.chat.MutableComponent name(ResourceKey<Enchantment> key) {
    return net.minecraft.network.chat.Component.translatable(net.minecraft.Util.makeDescriptionId("enchantment", key.location()));
  }
}
