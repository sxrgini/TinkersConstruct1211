package slimeknights.mantle.platform.client.model;

import com.mojang.math.Transformation;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.util.Mth;
import org.joml.Matrix3f;
import org.joml.Vector3f;
import org.joml.Vector4f;

/** Common quad transformers, replacing NeoForge's {@code QuadTransformers}. */
public final class QuadTransformers {
  private static final IQuadTransformer EMPTY = quad -> {};

  private QuadTransformers() {}

  /** Checks if the transformation does nothing */
  public static boolean isIdentity(Transformation transformation) {
    return transformation.equals(Transformation.identity());
  }

  /** Transformer that does nothing */
  public static IQuadTransformer empty() {
    return EMPTY;
  }

  /** Sets the lightmap of every vertex to at least the given emissivity */
  public static IQuadTransformer settingEmissivity(int emissivity) {
    return applyingLightmap(emissivity, emissivity);
  }

  /** Raises the block and sky light of every vertex to at least the given values */
  public static IQuadTransformer applyingLightmap(int blockLight, int skyLight) {
    return quad -> {
      int[] data = quad.getVertices();
      for (int i = 0; i < 4; i++) {
        int index = i * IQuadTransformer.STRIDE + IQuadTransformer.UV2;
        int existing = data[index];
        int block = Math.max(blockLight, LightTexture.block(existing));
        int sky = Math.max(skyLight, LightTexture.sky(existing));
        data[index] = LightTexture.pack(block, sky);
      }
    };
  }

  /**
   * Applies a transformation to positions and normals. The transformation is applied around the center of the block (0.5, 0.5, 0.5).
   */
  public static IQuadTransformer applying(Transformation transformation) {
    if (QuadTransformers.isIdentity(transformation)) {
      return EMPTY;
    }
    Matrix3f normalMatrix = Transformations.normalMatrix(transformation);
    return quad -> {
      int[] data = quad.getVertices();
      Vector4f position = new Vector4f();
      Vector3f normal = new Vector3f();
      for (int i = 0; i < 4; i++) {
        int base = i * IQuadTransformer.STRIDE;
        position.set(Float.intBitsToFloat(data[base]) - 0.5f, Float.intBitsToFloat(data[base + 1]) - 0.5f, Float.intBitsToFloat(data[base + 2]) - 0.5f, 1f);
        transformation.getMatrix().transform(position);
        data[base] = Float.floatToRawIntBits(position.x() + 0.5f);
        data[base + 1] = Float.floatToRawIntBits(position.y() + 0.5f);
        data[base + 2] = Float.floatToRawIntBits(position.z() + 0.5f);

        int packed = data[base + IQuadTransformer.NORMAL];
        normal.set(((byte) (packed & 0xFF)) / 127f, ((byte) ((packed >> 8) & 0xFF)) / 127f, ((byte) ((packed >> 16) & 0xFF)) / 127f);
        normalMatrix.transform(normal);
        normal.normalize();
        data[base + IQuadTransformer.NORMAL] = (packed & 0xFF000000)
          | ((((byte) Mth.clamp(Math.round(normal.x() * 127f), -127, 127)) & 0xFF))
          | ((((byte) Mth.clamp(Math.round(normal.y() * 127f), -127, 127)) & 0xFF) << 8)
          | ((((byte) Mth.clamp(Math.round(normal.z() * 127f), -127, 127)) & 0xFF) << 16);
      }
    };
  }
}
