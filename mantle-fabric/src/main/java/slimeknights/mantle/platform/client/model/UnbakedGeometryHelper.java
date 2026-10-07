package slimeknights.mantle.platform.client.model;

import com.mojang.math.Transformation;
import net.minecraft.client.resources.model.ModelState;
import org.joml.Vector3f;

/** Helpers for applying root transforms, replacing parts of NeoForge's {@code UnbakedGeometryHelper}. */
public final class UnbakedGeometryHelper {
  private static final Vector3f CENTER = new Vector3f(0.5f, 0.5f, 0.5f);

  private UnbakedGeometryHelper() {}

  /**
   * Gets a quad transformer applying the root transform to quads already baked with the model state.
   * The state rotation is conjugated out so the root transform acts in the unrotated model space.
   */
  public static IQuadTransformer applyRootTransform(ModelState state, Transformation rootTransform) {
    if (QuadTransformers.isIdentity(rootTransform)) {
      return QuadTransformers.empty();
    }
    Transformation stateRotation = state.getRotation();
    if (QuadTransformers.isIdentity(stateRotation)) {
      return QuadTransformers.applying(rootTransform);
    }
    Transformation inverse = stateRotation.inverse();
    return QuadTransformers.applying(stateRotation.compose(rootTransform).compose(inverse));
  }

  /** Composes the root transform into the model state, for geometry built directly in model space such as item layers. */
  public static ModelState composeRootTransformIntoModelState(ModelState state, Transformation rootTransform) {
    Transformation composed = Transformations.applyOrigin(state.getRotation(), CENTER).compose(Transformations.applyOrigin(rootTransform, CENTER));
    return new SimpleModelState(composed, state.isUvLocked());
  }
}
