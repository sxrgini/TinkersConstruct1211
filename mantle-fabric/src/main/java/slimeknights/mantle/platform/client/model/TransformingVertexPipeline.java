package slimeknights.mantle.platform.client.model;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Transformation;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

/** Vertex consumer that transforms positions and normals before passing them on. The transform is applied as is, without centering. */
public class TransformingVertexPipeline implements VertexConsumer {
  private final VertexConsumer parent;
  private final Matrix4f matrix;
  private final Matrix3f normalMatrix;

  public TransformingVertexPipeline(VertexConsumer parent, Transformation transformation) {
    this.parent = parent;
    this.matrix = transformation.getMatrix();
    this.normalMatrix = Transformations.normalMatrix(transformation);
  }

  @Override
  public VertexConsumer addVertex(float x, float y, float z) {
    Vector4f pos = matrix.transform(new Vector4f(x, y, z, 1f));
    parent.addVertex(pos.x(), pos.y(), pos.z());
    return this;
  }

  @Override
  public VertexConsumer setColor(int red, int green, int blue, int alpha) {
    parent.setColor(red, green, blue, alpha);
    return this;
  }

  @Override
  public VertexConsumer setUv(float u, float v) {
    parent.setUv(u, v);
    return this;
  }

  @Override
  public VertexConsumer setUv1(int u, int v) {
    parent.setUv1(u, v);
    return this;
  }

  @Override
  public VertexConsumer setUv2(int u, int v) {
    parent.setUv2(u, v);
    return this;
  }

  @Override
  public VertexConsumer setNormal(float x, float y, float z) {
    Vector3f normal = normalMatrix.transform(new Vector3f(x, y, z));
    normal.normalize();
    parent.setNormal(normal.x(), normal.y(), normal.z());
    return this;
  }
}
