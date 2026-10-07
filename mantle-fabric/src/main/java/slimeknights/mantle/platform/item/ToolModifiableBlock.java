package slimeknights.mantle.platform.item;

import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

/** Implement on blocks to customize the result of tools using abilities on them, replacing NeoForge's block extension. */
public interface ToolModifiableBlock {
  /** Gets the state after performing the ability, or null if the ability does nothing. */
  @Nullable
  BlockState getToolModifiedState(BlockState state, UseOnContext context, ItemAbility ability, boolean simulate);
}
