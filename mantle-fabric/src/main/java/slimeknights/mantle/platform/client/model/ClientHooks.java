package slimeknights.mantle.platform.client.model;

import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import org.joml.Vector3f;

/** Replacement for the parts of NeoForge's {@code ClientHooks} Mantle uses */
public final class ClientHooks {
  private ClientHooks() {}

  /** Packs a normal into the vertex format */
  public static int packNormal(float x, float y, float z) {
    return (((byte) Mth.clamp(Math.round(x * 127f), -127, 127)) & 0xFF)
      | ((((byte) Mth.clamp(Math.round(y * 127f), -127, 127)) & 0xFF) << 8)
      | ((((byte) Mth.clamp(Math.round(z * 127f), -127, 127)) & 0xFF) << 16);
  }

  /** Computes the face normal of the quad vertex data and writes it into every vertex */
  public static void fillNormal(int[] data, Direction facing) {
    Vector3f p0 = pos(data, 0);
    Vector3f p1 = pos(data, 1);
    Vector3f p2 = pos(data, 2);
    Vector3f p3 = pos(data, 3);
    Vector3f v1 = new Vector3f(p3).sub(p1);
    Vector3f v2 = new Vector3f(p2).sub(p0);
    Vector3f normal = v1.cross(v2);
    if (normal.lengthSquared() == 0) {
      normal.set(facing.getStepX(), facing.getStepY(), facing.getStepZ());
    }
    normal.normalize();
    int packed = packNormal(normal.x(), normal.y(), normal.z());
    for (int i = 0; i < 4; i++) {
      data[i * IQuadTransformer.STRIDE + IQuadTransformer.NORMAL] = packed;
    }
  }

  private static Vector3f pos(int[] data, int vertex) {
    int base = vertex * IQuadTransformer.STRIDE;
    return new Vector3f(Float.intBitsToFloat(data[base]), Float.intBitsToFloat(data[base + 1]), Float.intBitsToFloat(data[base + 2]));
  }
}
