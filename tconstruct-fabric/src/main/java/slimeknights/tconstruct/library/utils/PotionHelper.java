package slimeknights.tconstruct.library.utils;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentHolder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

/** Replacement for the removed vanilla {@code PotionUtils}, working on anything holding data components (items, fluids). */
public final class PotionHelper {
  private PotionHelper() {}

  /** Gets the potion contents of the holder, empty if missing */
  public static PotionContents contents(DataComponentHolder holder) {
    return holder.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
  }

  /** Gets the potion of the holder, or null if missing */
  @Nullable
  public static Holder<Potion> getPotion(DataComponentHolder holder) {
    return contents(holder).potion().orElse(null);
  }

  /** Gets the ID of the potion on the holder, or an empty string if none */
  public static String getPotionId(DataComponentHolder holder) {
    return contents(holder).potion().flatMap(Holder::unwrapKey).map(key -> key.location().toString()).orElse("");
  }

  /** Looks up a potion by ID string, null if invalid */
  @Nullable
  public static Holder<Potion> byId(String id) {
    ResourceLocation location = ResourceLocation.tryParse(id);
    if (location != null) {
      return BuiltInRegistries.POTION.getHolder(location).orElse(null);
    }
    return null;
  }

  /** Sets the potion on the stack, returning it */
  public static ItemStack setPotion(ItemStack stack, Holder<Potion> potion) {
    stack.set(DataComponents.POTION_CONTENTS, new PotionContents(potion));
    return stack;
  }

  /** Gets the color of the potion on the holder */
  public static int getColor(DataComponentHolder holder) {
    return contents(holder).getColor();
  }

  /** Gets the name key of the potion with the given prefix */
  public static String getName(DataComponentHolder holder, String prefix) {
    return Potion.getName(contents(holder).potion(), prefix);
  }

  /** Adds the potion tooltip for the holder */
  public static void addTooltip(DataComponentHolder holder, Consumer<Component> consumer, float durationScale, float tickRate) {
    contents(holder).addPotionTooltip(consumer, durationScale, tickRate);
  }

  /** Gets all effects of the holder */
  public static List<MobEffectInstance> getEffects(DataComponentHolder holder) {
    List<MobEffectInstance> list = new java.util.ArrayList<>();
    contents(holder).getAllEffects().forEach(list::add);
    return list;
  }

  /** Checks if the potion is the empty potion */
  public static boolean isEmpty(@Nullable DataComponentHolder holder) {
    return holder == null || contents(holder).potion().isEmpty();
  }

  /** Checks if the holder contains the water potion */
  public static boolean isWater(DataComponentHolder holder) {
    return contents(holder).is(net.minecraft.world.item.alchemy.Potions.WATER);
  }
}
