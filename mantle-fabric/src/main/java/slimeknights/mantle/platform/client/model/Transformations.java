package slimeknights.mantle.platform.client.model;

import com.mojang.math.Transformation;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;

/** Replacements for NeoForge extensions on {@link Transformation} */
public final class Transformations {
  private Transformations() {}

  /** Gets the matrix used to transform normals, the inverse transpose of the linear part */
  public static Matrix3f normalMatrix(Transformation transformation) {
    return new Matrix3f(transformation.getMatrix()).invert().transpose();
  }

  /** Applies the transformation around the given origin */
  public static Transformation applyOrigin(Transformation transformation, Vector3f origin) {
    if (QuadTransformers.isIdentity(transformation)) {
      return transformation;
    }
    Matrix4f matrix = new Matrix4f().translate(origin).mul(transformation.getMatrix()).translate(-origin.x(), -origin.y(), -origin.z());
    return new Transformation(matrix);
  }
}
