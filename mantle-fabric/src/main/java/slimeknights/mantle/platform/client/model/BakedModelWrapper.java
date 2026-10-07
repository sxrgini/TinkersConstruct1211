package slimeknights.mantle.platform.client.model;

import net.fabricmc.fabric.api.renderer.v1.Renderer;
import net.fabricmc.fabric.api.renderer.v1.material.RenderMaterial;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.renderer.v1.model.ForwardingBakedModel;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Supplier;

/**
 * Base for baked models that need {@link ModelData}. Mirrors NeoForge's {@code BakedModelWrapper} plus its dynamic model hooks, and bridges them to the Fabric renderer API:
 * block entity render data that is a {@link ModelData} is passed to {@link #getQuads(BlockState, Direction, RandomSource, ModelData, RenderType)}.
 */
public class BakedModelWrapper<T extends BakedModel> extends ForwardingBakedModel {
  protected final T originalModel;

  public BakedModelWrapper(T originalModel) {
    super(originalModel);
    this.originalModel = originalModel;
  }

  /** Gets model data for the position, called before quads are fetched. By default returns the block entity data. */
  public ModelData getModelData(BlockAndTintGetter level, BlockPos pos, BlockState state, ModelData blockEntityData) {
    return blockEntityData;
  }

  /** Gets the quads with model data, by default delegating to the wrapped model */
  public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource random, ModelData data, @Nullable RenderType renderType) {
    return BakedModels.getQuads(originalModel, state, side, random, data);
  }

  /** Gets the particle icon with model data */
  public TextureAtlasSprite getParticleIcon(ModelData data) {
    return BakedModels.getParticleIcon(originalModel, data);
  }

  @Override
  public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource random) {
    return getQuads(state, side, random, ModelData.EMPTY, null);
  }

  @Override
  public boolean isVanillaAdapter() {
    return false;
  }

  @Override
  public void emitBlockQuads(BlockAndTintGetter blockView, BlockState state, BlockPos pos, Supplier<RandomSource> randomSupplier, RenderContext context) {
    ModelData data = blockView.getBlockEntityRenderData(pos) instanceof ModelData blockEntityData ? blockEntityData : ModelData.EMPTY;
    emitQuads(state, randomSupplier, getModelData(blockView, pos, state, data), context);
  }

  @Override
  public void emitItemQuads(ItemStack stack, Supplier<RandomSource> randomSupplier, RenderContext context) {
    emitQuads(null, randomSupplier, ModelData.EMPTY, context);
  }

  private void emitQuads(@Nullable BlockState state, Supplier<RandomSource> randomSupplier, ModelData data, RenderContext context) {
    QuadEmitter emitter = context.getEmitter();
    RenderMaterial material = Renderer.get().materialFinder().find();
    RandomSource random = randomSupplier.get();
    for (int i = 0; i <= Direction.values().length; i++) {
      Direction side = i == Direction.values().length ? null : Direction.values()[i];
      for (BakedQuad quad : getQuads(state, side, random, data, null)) {
        emitter.fromVanilla(quad, material, side);
        emitter.emit();
      }
    }
  }
}
