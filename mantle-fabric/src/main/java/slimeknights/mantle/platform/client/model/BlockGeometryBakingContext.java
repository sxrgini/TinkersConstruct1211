package slimeknights.mantle.platform.client.model;

import com.mojang.math.Transformation;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.resources.model.Material;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;

/** Geometry baking context backed by a vanilla {@link BlockModel}, which handles parents and textures. */
public class BlockGeometryBakingContext implements IGeometryBakingContext {
  /** Underlying block model */
  public final BlockModel owner;
  private final String name;
  private final Transformation rootTransform;
  @Nullable
  private final ResourceLocation renderTypeHint;

  public BlockGeometryBakingContext(BlockModel owner, String name, Transformation rootTransform, @Nullable ResourceLocation renderTypeHint) {
    this.owner = owner;
    this.name = name;
    this.rootTransform = rootTransform;
    this.renderTypeHint = renderTypeHint;
  }

  @Override
  public String getModelName() {
    return name;
  }

  @Override
  public boolean hasMaterial(String name) {
    return !MissingTextureAtlasSprite.getLocation().equals(getMaterial(name).texture());
  }

  @Override
  public Material getMaterial(String name) {
    return owner.getMaterial(name);
  }

  @Override
  public boolean isGui3d() {
    return owner.getGuiLight().lightLikeBlock();
  }

  @Override
  public boolean useBlockLight() {
    return owner.getGuiLight().lightLikeBlock();
  }

  @Override
  public boolean useAmbientOcclusion() {
    return owner.hasAmbientOcclusion();
  }

  @Override
  public ItemTransforms getTransforms() {
    return owner.getTransforms();
  }

  @Override
  public Transformation getRootTransform() {
    return rootTransform;
  }

  @Nullable
  @Override
  public ResourceLocation getRenderTypeHint() {
    return renderTypeHint;
  }

  @Override
  public boolean isComponentVisible(String component, boolean fallback) {
    return fallback;
  }
}
