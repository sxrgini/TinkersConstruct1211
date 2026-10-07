package slimeknights.mantle.block;

import net.fabricmc.fabric.api.registry.StrippableBlockRegistry;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RotatedPillarBlock;

import java.util.function.Supplier;

/** Log block that can be stripped. Stripping is registered with Fabric once the block is registered, see {@link slimeknights.mantle.Mantle}. */
public class StrippableLogBlock extends RotatedPillarBlock {
  private final Supplier<? extends Block> stripped;
  public StrippableLogBlock(Supplier<? extends Block> stripped, Properties properties) {
    super(properties);
    this.stripped = stripped;
  }

  /** Registers the stripping behavior, called after this block is registered */
  public void registerStripping() {
    StrippableBlockRegistry.register(this, stripped.get());
  }
}
