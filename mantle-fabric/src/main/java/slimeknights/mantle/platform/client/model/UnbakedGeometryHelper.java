package slimeknights.mantle.platform.client.model;

import com.mojang.math.Transformation;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockElement;
import net.minecraft.client.renderer.block.model.BlockElementFace;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.ItemModelGenerator;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.Material;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

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

  private static final ItemModelGenerator ITEM_MODEL_GENERATOR = new ItemModelGenerator();

  /** Creates the elements of an item layer in the shape of the given sprite, with the tint index set to the layer */
  public static List<BlockElement> createUnbakedItemElements(int layerIndex, SpriteContents sprite) {
    return new ArrayList<>(ITEM_MODEL_GENERATOR.processFrames(layerIndex, "layer" + layerIndex, sprite));
  }

  /** Creates the elements of an item layer without the front and back faces, so only the sides remain. Used for masking. */
  public static List<BlockElement> createUnbakedItemMaskElements(int layerIndex, SpriteContents sprite) {
    List<BlockElement> elements = createUnbakedItemElements(layerIndex, sprite);
    // the first element is the front and back face
    elements.remove(0);
    return elements;
  }

  /** Bakes the elements into quads using the given sprite getter */
  public static List<BakedQuad> bakeElements(List<BlockElement> elements, Function<Material,TextureAtlasSprite> spriteGetter, ModelState state) {
    if (elements.isEmpty()) {
      return List.of();
    }
    List<BakedQuad> quads = new ArrayList<>();
    for (BlockElement element : elements) {
      for (Direction direction : element.faces.keySet()) {
        BlockElementFace face = element.faces.get(direction);
        String texture = face.texture();
        if (texture.startsWith("#")) {
          texture = texture.substring(1);
        }
        TextureAtlasSprite sprite = spriteGetter.apply(new Material(InventoryMenu.BLOCK_ATLAS, ResourceLocation.withDefaultNamespace(texture)));
        quads.add(BlockModel.bakeFace(element, face, sprite, direction, state));
      }
    }
    return quads;
  }
}
