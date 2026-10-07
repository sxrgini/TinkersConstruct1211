package slimeknights.mantle.platform.item;

import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;

/** Built in abilities and lookup, replacing NeoForge's {@code ItemAbilities}. */
public final class ItemAbilities {
  /** Ability to perform a sweep attack, vanilla swords can by default */
  public static final ItemAbility SWORD_SWEEP = ItemAbility.get("sword_sweep");

  public static final ItemAbility AXE_DIG = ItemAbility.get("axe_dig");
  public static final ItemAbility AXE_STRIP = ItemAbility.get("axe_strip");
  public static final ItemAbility AXE_SCRAPE = ItemAbility.get("axe_scrape");
  public static final ItemAbility AXE_WAX_OFF = ItemAbility.get("axe_wax_off");
  public static final ItemAbility FISHING_ROD_CAST = ItemAbility.get("fishing_rod_cast");
  public static final ItemAbility HOE_DIG = ItemAbility.get("hoe_dig");
  public static final ItemAbility HOE_TILL = ItemAbility.get("till");
  public static final ItemAbility PICKAXE_DIG = ItemAbility.get("pickaxe_dig");
  public static final ItemAbility SHEARS_CARVE = ItemAbility.get("shears_carve");
  public static final ItemAbility SHEARS_DIG = ItemAbility.get("shears_dig");
  public static final ItemAbility SHEARS_DISARM = ItemAbility.get("shears_disarm");
  public static final ItemAbility SHEARS_HARVEST = ItemAbility.get("shears_harvest");
  public static final ItemAbility SHIELD_BLOCK = ItemAbility.get("shield_block");
  public static final ItemAbility SHOVEL_DIG = ItemAbility.get("shovel_dig");
  public static final ItemAbility SHOVEL_FLATTEN = ItemAbility.get("shovel_flatten");
  public static final ItemAbility SWORD_DIG = ItemAbility.get("sword_dig");

  private ItemAbilities() {}

  /** Checks if the stack can perform the ability */
  public static boolean canPerform(ItemStack stack, ItemAbility ability) {
    if (stack.getItem() instanceof ItemAbilityProvider provider) {
      return provider.canPerformAction(stack, ability);
    }
    net.minecraft.world.item.Item item = stack.getItem();
    if (ability == SWORD_SWEEP || ability == SWORD_DIG) {
      return stack.is(ItemTags.SWORDS);
    } else if (ability == PICKAXE_DIG) {
      return stack.is(ItemTags.PICKAXES);
    } else if (ability == AXE_DIG || ability == AXE_STRIP || ability == AXE_SCRAPE || ability == AXE_WAX_OFF) {
      return stack.is(ItemTags.AXES);
    } else if (ability == SHOVEL_DIG || ability == SHOVEL_FLATTEN) {
      return stack.is(ItemTags.SHOVELS);
    } else if (ability == HOE_DIG || ability == HOE_TILL) {
      return stack.is(ItemTags.HOES);
    } else if (ability == SHEARS_DIG || ability == SHEARS_HARVEST || ability == SHEARS_CARVE || ability == SHEARS_DISARM) {
      return item instanceof net.minecraft.world.item.ShearsItem;
    } else if (ability == SHIELD_BLOCK) {
      return item instanceof net.minecraft.world.item.ShieldItem;
    } else if (ability == FISHING_ROD_CAST) {
      return item instanceof net.minecraft.world.item.FishingRodItem;
    }
    return false;
  }
}
