package slimeknights.mantle.platform.registry;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/** Holder for a block. */
public class DeferredBlock<T extends Block> extends DeferredHolder<Block,T> implements ItemLike {
  protected DeferredBlock(ResourceKey<Block> key) {
    super(key);
  }

  /** Creates a holder for the given block key */
  public static <T extends Block> DeferredBlock<T> createBlock(ResourceKey<Block> key) {
    return new DeferredBlock<>(key);
  }

  @Override
  public Item asItem() {
    return get().asItem();
  }

  /** Gets the default state of the block */
  public BlockState defaultBlockState() {
    return get().defaultBlockState();
  }
}
