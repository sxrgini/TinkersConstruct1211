package slimeknights.mantle.platform.client.model;

import com.mojang.math.Transformation;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.resources.model.Material;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;

/** Context passed to geometry when baking, replacing NeoForge's {@code IGeometryBakingContext}. */
public interface IGeometryBakingContext {
  /** Gets the name of the model being baked */
  String getModelName();

  boolean hasMaterial(String name);

  Material getMaterial(String name);

  boolean isGui3d();

  boolean useBlockLight();

  boolean useAmbientOcclusion();

  ItemTransforms getTransforms();

  Transformation getRootTransform();

  @Nullable
  ResourceLocation getRenderTypeHint();

  boolean isComponentVisible(String component, boolean fallback);

  /** Gets the render type group for the given name, always empty on Fabric */
  default RenderTypeGroup getRenderType(ResourceLocation name) {
    return RenderTypeGroup.EMPTY;
  }
}
