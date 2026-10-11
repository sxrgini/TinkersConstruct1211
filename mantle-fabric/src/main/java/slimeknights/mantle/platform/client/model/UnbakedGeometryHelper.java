package slimeknights.mantle.platform.client.model;

import com.mojang.math.Transformation;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockElement;
import net.minecraft.client.renderer.block.model.BlockElementFace;
import net.minecraft.client.renderer.block.model.BlockFaceUV;
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
import java.util.EnumMap;
import java.util.Map;
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

  /** Creates elements covering exactly the opaque pixels of the sprite, one front and back strip per run of pixels. Used for masking. */
  public static List<BlockElement> createUnbakedItemMaskElements(int layerIndex, SpriteContents sprite) {
    List<BlockElement> elements = new ArrayList<>();
    int width = sprite.width();
    int height = sprite.height();
    float xScale = 16f / width;
    float yScale = 16f / height;
    String texture = "layer" + layerIndex;
    for (int y = 0; y < height; y++) {
      int x = 0;
      while (x < width) {
        if (sprite.isTransparent(0, x, y)) {
          x++;
          continue;
        }
        int start = x;
        while (x < width && !sprite.isTransparent(0, x, y)) {
          x++;
        }
        float[] uv = {start * xScale, y * yScale, x * xScale, (y + 1) * yScale};
        Map<Direction,BlockElementFace> faces = new EnumMap<>(Direction.class);
        faces.put(Direction.NORTH, new BlockElementFace(null, layerIndex, texture, new BlockFaceUV(uv.clone(), 0)));
        faces.put(Direction.SOUTH, new BlockElementFace(null, layerIndex, texture, new BlockFaceUV(uv.clone(), 0)));
        elements.add(new BlockElement(new Vector3f(start * xScale, 16 - (y + 1) * yScale, 7.5f), new Vector3f(x * xScale, 16 - y * yScale, 8.5f), faces, null, true));
      }
    }
    return elements;
  }

  /** Bakes the elements into quads using the given sprite getter */
  /** Overload with the model location, which is unused as quad baking no longer takes it */
  public static List<BakedQuad> bakeElements(List<BlockElement> elements, Function<Material,TextureAtlasSprite> spriteGetter, ModelState state, net.minecraft.resources.ResourceLocation modelLocation) {
    return bakeElements(elements, spriteGetter, state);
  }

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
