package slimeknights.tconstruct.library.utils;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

import javax.annotation.Nullable;

/**
 * Compatibility layer for the old stack NBT, backed by the custom data component.
 * Tags returned are live, edits to them apply to the stack. {@link slimeknights.tconstruct.mixin.ItemStackMixin} deep copies the data when a stack is copied so edits do not alias.
 */
public final class StackNbt {
  private StackNbt() {}

  /** Wraps the tag in custom data without copying and without collapsing empty tags */
  public static CustomData wrap(CompoundTag tag) {
    return new CustomData(tag);
  }

  /** Gets the live tag of the stack, or null if there is none */
  @Nullable
  public static CompoundTag getTag(ItemStack stack) {
    CustomData data = stack.get(DataComponents.CUSTOM_DATA);
    return data == null ? null : data.getUnsafe();
  }

  /** Gets the live tag of the stack, creating it if missing */
  public static CompoundTag getOrCreateTag(ItemStack stack) {
    CustomData data = stack.get(DataComponents.CUSTOM_DATA);
    if (data != null) {
      return data.getUnsafe();
    }
    CompoundTag tag = new CompoundTag();
    stack.set(DataComponents.CUSTOM_DATA, wrap(tag));
    return tag;
  }

  /** Checks if the stack has a non-empty tag */
  public static boolean hasTag(ItemStack stack) {
    CustomData data = stack.get(DataComponents.CUSTOM_DATA);
    return data != null && !data.isEmpty();
  }

  /** Sets the tag on the stack without copying, removing it if null */
  public static void setTag(ItemStack stack, @Nullable CompoundTag tag) {
    if (tag == null) {
      stack.remove(DataComponents.CUSTOM_DATA);
    } else {
      stack.set(DataComponents.CUSTOM_DATA, wrap(tag));
    }
  }
}
