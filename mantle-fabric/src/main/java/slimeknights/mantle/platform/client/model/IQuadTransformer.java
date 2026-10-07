package slimeknights.mantle.platform.client.model;

import net.minecraft.client.renderer.block.model.BakedQuad;

import java.util.List;

/** Modifies baked quad vertex data in place, mirroring NeoForge's {@code IQuadTransformer}. */
@FunctionalInterface
public interface IQuadTransformer {
  /** Number of ints per vertex in the block vertex format */
  int STRIDE = 8;
  /** Offset of the position (3 floats) */
  int POSITION = 0;
  /** Offset of the packed ABGR color */
  int COLOR = 3;
  /** Offset of the texture UV (2 floats) */
  int UV0 = 4;
  /** Offset of the packed lightmap UV */
  int UV2 = 6;
  /** Offset of the packed normal */
  int NORMAL = 7;

  /** Modifies the quad in place */
  void processInPlace(BakedQuad quad);

  /** Modifies all quads in place */
  default void processInPlace(List<BakedQuad> quads) {
    for (BakedQuad quad : quads) {
      processInPlace(quad);
    }
  }

  /** Runs this transformer, then the next */
  default IQuadTransformer andThen(IQuadTransformer next) {
    return quad -> {
      processInPlace(quad);
      next.processInPlace(quad);
    };
  }
}
