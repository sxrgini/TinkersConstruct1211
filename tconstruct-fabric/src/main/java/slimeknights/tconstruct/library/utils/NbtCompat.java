package slimeknights.tconstruct.library.utils;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;

/** Helpers for NBT formats vanilla changed in 1.21 that we still use for our own data */
public final class NbtCompat {
  private NbtCompat() {}

  /** Writes a block position as a compound with X, Y, and Z */
  public static CompoundTag writeBlockPos(BlockPos pos) {
    CompoundTag tag = new CompoundTag();
    tag.putInt("X", pos.getX());
    tag.putInt("Y", pos.getY());
    tag.putInt("Z", pos.getZ());
    return tag;
  }

  /** Reads a block position from a compound with X, Y, and Z */
  public static BlockPos readBlockPos(CompoundTag tag) {
    return new BlockPos(tag.getInt("X"), tag.getInt("Y"), tag.getInt("Z"));
  }
}
