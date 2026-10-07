package slimeknights.mantle.platform.client.model;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.List;

/** Helpers for fetching quads from any model with model data. Plain vanilla models ignore the data. */
public final class BakedModels {
  private BakedModels() {}

  /** Gets quads from the model, passing in model data if supported */
  public static List<BakedQuad> getQuads(BakedModel model, @Nullable BlockState state, @Nullable Direction side, RandomSource random, ModelData data) {
    if (model instanceof BakedModelWrapper<?> wrapper) {
      return wrapper.getQuads(state, side, random, data, null);
    }
    return model.getQuads(state, side, random);
  }

  /** Gets the particle icon from the model, passing in model data if supported */
  public static TextureAtlasSprite getParticleIcon(BakedModel model, ModelData data) {
    if (model instanceof BakedModelWrapper<?> wrapper && wrapper.getClass() != BakedModelWrapper.class) {
      return wrapper.getParticleIcon(data);
    }
    return model.getParticleIcon();
  }
}
