package slimeknights.mantle.item.burnable;

import net.minecraft.world.item.HangingSignItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Block;

import javax.annotation.Nullable;

/**
 * Hanging sign item with a burn time. Used in {@link slimeknights.mantle.registration.object.WoodBlockObject} registration.
 * @see BurnableBlockItem
 * @see BurnableSignItem
 */
public class BurnableHangingSignItem extends HangingSignItem {
  private final int burnTime;
  public BurnableHangingSignItem(Properties properties, Block hangingBlock, Block wallBlock, int burnTime) {
    super(hangingBlock, wallBlock, properties);
    this.burnTime = burnTime;
    net.fabricmc.fabric.api.registry.FuelRegistry.INSTANCE.add(this, burnTime);
  }

}
