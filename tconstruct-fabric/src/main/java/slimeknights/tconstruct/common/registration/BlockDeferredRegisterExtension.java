package slimeknights.tconstruct.common.registration;

import slimeknights.mantle.registration.object.MetalItemObject;
import slimeknights.mantle.registration.object.FenceBuildingBlockObject;
import slimeknights.mantle.registration.object.WallBuildingBlockObject;
import slimeknights.mantle.registration.object.BuildingBlockObject;
import slimeknights.mantle.platform.registry.DeferredBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.item.BlockItem;
import java.util.function.Supplier;
import java.util.function.Function;
import slimeknights.mantle.platform.registry.DeferredHolder;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.MapColor;
import slimeknights.mantle.registration.deferred.BlockDeferredRegister;
import slimeknights.tconstruct.common.registration.GeodeItemObject.BudSize;

import java.util.Map;

/** Additional methods in deferred register extension */
@SuppressWarnings("UnusedReturnValue")
public class BlockDeferredRegisterExtension extends BlockDeferredRegister {
  public BlockDeferredRegisterExtension(String modID) {
    super(modID);
  }

  /**
   * Registers a geode block
   * @param name         Geode name
   * @param color        Color of the geode
   * @param blockSound   Sound of the block and budding block
   * @param props        Item props
   * @return The geode block
   */
  public GeodeItemObject registerGeode(String name, MapColor color, SoundType blockSound, SoundEvent chimeSound, Map<BudSize,SoundType> clusterSounds, int baseLight, Item.Properties props) {
    DeferredHolder<Item, Item> shard = itemRegister.register(name, () -> new Item(props));
    return new GeodeItemObject(shard, this, color, blockSound, chimeSound, clusterSounds, baseLight, props);
  }

  /* Overloads matching the old Mantle API, which took a block supplier and copied the properties for slabs and stairs */

  /** Registers a block with a slab and stairs, copying the properties of the main block */
  public BuildingBlockObject registerBuilding(String name, Supplier<? extends Block> block, Function<? super Block, ? extends BlockItem> item) {
    DeferredBlock<? extends Block> main = register(name, block, item);
    DeferredBlock<SlabBlock> slab = register(name + "_slab", () -> new SlabBlock(BlockBehaviour.Properties.ofFullCopy(main.get())), item);
    DeferredBlock<StairBlock> stairs = register(name + "_stairs", () -> new StairBlock(main.get().defaultBlockState(), BlockBehaviour.Properties.ofFullCopy(main.get())), item);
    return new BuildingBlockObject(main, slab, stairs);
  }

  /** Registers a block with a slab, stairs and wall */
  public WallBuildingBlockObject registerWallBuilding(String name, Supplier<? extends Block> block, Function<? super Block, ? extends BlockItem> item) {
    BuildingBlockObject building = registerBuilding(name, block, item);
    return new WallBuildingBlockObject(building, register(name + "_wall", () -> new WallBlock(BlockBehaviour.Properties.ofFullCopy(building.get())), item));
  }

  /** Registers a block with a slab, stairs and fence */
  public FenceBuildingBlockObject registerFenceBuilding(String name, Supplier<? extends Block> block, Function<? super Block, ? extends BlockItem> item) {
    BuildingBlockObject building = registerBuilding(name, block, item);
    return new FenceBuildingBlockObject(building, register(name + "_fence", () -> new FenceBlock(BlockBehaviour.Properties.ofFullCopy(building.get())), item));
  }

  /** Registers a block with a slab, stairs and fence from properties */
  public FenceBuildingBlockObject registerFenceBuilding(String name, BlockBehaviour.Properties props, Function<? super Block, ? extends BlockItem> item) {
    return registerFenceBuilding(name, () -> new Block(props), item);
  }

  /** Registers a block with a slab and stairs from properties */
  public BuildingBlockObject registerBuilding(String name, BlockBehaviour.Properties props, Function<? super Block, ? extends BlockItem> item) {
    return registerBuilding(name, () -> new Block(props), item);
  }

  /** Registers a metal block, ingot and nugget from properties */
  public MetalItemObject registerMetal(String name, BlockBehaviour.Properties props, Function<Block, ? extends BlockItem> blockItem, Item.Properties itemProps) {
    return registerMetal(name).blockItem(blockItem).ingotNugget(itemProps).block(props);
  }

  /** Registers a metal block, ingot and nugget from a block supplier */
  public MetalItemObject registerMetal(String name, Supplier<Block> block, Function<Block, ? extends BlockItem> blockItem, Item.Properties itemProps) {
    return registerMetal(name).blockItem(blockItem).ingotNugget(itemProps).block(block);
  }
}
