package slimeknights.mantle.platform.fluid;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;

import javax.annotation.Nullable;
import java.util.function.Supplier;

/** Flowing fluid driven by a {@link Properties} object, replacing NeoForge's {@code BaseFlowingFluid}. */
public abstract class BaseFlowingFluid extends FlowingFluid implements FluidTypeProvider {
  private final Properties properties;

  protected BaseFlowingFluid(Properties properties) {
    this.properties = properties;
    registerDefaultState(getStateDefinition().any());
  }

  @Override
  public FluidType getFluidType() {
    return properties.type.get();
  }

  @Override
  public Fluid getFlowing() {
    return properties.flowing.get();
  }

  @Override
  public Fluid getSource() {
    return properties.still.get();
  }

  @Override
  protected boolean canConvertToSource(Level level) {
    return getFluidType().canConvertToSource();
  }

  @Override
  protected void beforeDestroyingBlock(LevelAccessor level, BlockPos pos, BlockState state) {
    BlockEntity blockEntity = state.hasBlockEntity() ? level.getBlockEntity(pos) : null;
    Block.dropResources(state, level, pos, blockEntity);
  }

  @Override
  protected int getSlopeFindDistance(LevelReader level) {
    return properties.slopeFindDistance;
  }

  @Override
  protected int getDropOff(LevelReader level) {
    return properties.levelDecreasePerBlock;
  }

  @Override
  public Item getBucket() {
    return properties.bucket != null ? properties.bucket.get() : Items.AIR;
  }

  @Override
  protected boolean canBeReplacedWith(FluidState state, BlockGetter level, BlockPos pos, Fluid fluid, Direction direction) {
    return direction == Direction.DOWN && !isSame(fluid);
  }

  @Override
  public int getTickDelay(LevelReader level) {
    return properties.tickRate;
  }

  @Override
  protected float getExplosionResistance() {
    return properties.explosionResistance;
  }

  @Override
  protected BlockState createLegacyBlock(FluidState state) {
    if (properties.block != null) {
      return properties.block.get().defaultBlockState().setValue(LiquidBlock.LEVEL, getLegacyLevel(state));
    }
    return Blocks.AIR.defaultBlockState();
  }

  @Override
  public boolean isSame(Fluid fluid) {
    return fluid == properties.still.get() || fluid == properties.flowing.get();
  }

  /** Still variant */
  public static class Source extends BaseFlowingFluid {
    public Source(Properties properties) {
      super(properties);
    }

    @Override
    public int getAmount(FluidState state) {
      return 8;
    }

    @Override
    public boolean isSource(FluidState state) {
      return true;
    }
  }

  /** Flowing variant */
  public static class Flowing extends BaseFlowingFluid {
    public Flowing(Properties properties) {
      super(properties);
      registerDefaultState(getStateDefinition().any().setValue(LEVEL, 7));
    }

    @Override
    protected void createFluidStateDefinition(StateDefinition.Builder<Fluid,FluidState> builder) {
      super.createFluidStateDefinition(builder);
      builder.add(LEVEL);
    }

    @Override
    public int getAmount(FluidState state) {
      return state.getValue(LEVEL);
    }

    @Override
    public boolean isSource(FluidState state) {
      return false;
    }
  }

  /** Properties for a flowing fluid, with suppliers to allow referencing things not yet registered */
  public static class Properties {
    private final Supplier<? extends FluidType> type;
    private final Supplier<? extends Fluid> still;
    private final Supplier<? extends Fluid> flowing;
    @Nullable
    private Supplier<? extends LiquidBlock> block;
    @Nullable
    private Supplier<? extends Item> bucket;
    private int slopeFindDistance = 4;
    private int levelDecreasePerBlock = 1;
    private float explosionResistance = 1;
    private int tickRate = 5;

    public Properties(Supplier<? extends FluidType> type, Supplier<? extends Fluid> still, Supplier<? extends Fluid> flowing) {
      this.type = type;
      this.still = still;
      this.flowing = flowing;
    }

    public Properties block(@Nullable Supplier<? extends LiquidBlock> block) {
      this.block = block;
      return this;
    }

    public Properties bucket(@Nullable Supplier<? extends Item> bucket) {
      this.bucket = bucket;
      return this;
    }

    public Properties slopeFindDistance(int slopeFindDistance) {
      this.slopeFindDistance = slopeFindDistance;
      return this;
    }

    public Properties levelDecreasePerBlock(int levelDecreasePerBlock) {
      this.levelDecreasePerBlock = levelDecreasePerBlock;
      return this;
    }

    public Properties explosionResistance(float explosionResistance) {
      this.explosionResistance = explosionResistance;
      return this;
    }

    public Properties tickRate(int tickRate) {
      this.tickRate = tickRate;
      return this;
    }
  }
}
