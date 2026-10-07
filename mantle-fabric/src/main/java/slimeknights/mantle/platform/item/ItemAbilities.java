package slimeknights.mantle.platform.item;

import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;

/** Built in abilities and lookup, replacing NeoForge's {@code ItemAbilities}. */
public final class ItemAbilities {
  /** Ability to perform a sweep attack, vanilla swords can by default */
  public static final ItemAbility SWORD_SWEEP = ItemAbility.get("sword_sweep");

  private ItemAbilities() {}

  /** Checks if the stack can perform the ability */
  public static boolean canPerform(ItemStack stack, ItemAbility ability) {
    if (stack.getItem() instanceof ItemAbilityProvider provider) {
      return provider.canPerformAction(stack, ability);
    }
    return ability == SWORD_SWEEP && stack.is(ItemTags.SWORDS);
  }
}
