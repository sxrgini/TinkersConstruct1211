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
}
