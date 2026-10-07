package slimeknights.mantle.platform.item;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;

import java.util.List;

/** Block or entity that can be sheared, equivalent of Forge's IForgeShearable */
public interface IShearable {
  /** Checks if the object can be sheared */
  default boolean isShearable(ItemStack item, Level level, BlockPos pos) {
    return true;
  }

  /** Performs the shear, returning drops */
  default List<ItemStack> onSheared(net.minecraft.world.entity.player.Player player, ItemStack item, Level level, BlockPos pos) {
    return List.of();
  }
}
