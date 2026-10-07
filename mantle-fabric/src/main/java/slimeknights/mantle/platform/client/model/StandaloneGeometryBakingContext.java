package slimeknights.mantle.platform.client.model;

import com.mojang.math.Transformation;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.resources.model.Material;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

/** Baking context not tied to a model file, with a builder for copies of another context */
public class StandaloneGeometryBakingContext implements IGeometryBakingContext {
  private final IGeometryBakingContext parent;
  private final boolean gui3d;
  private final boolean blockLight;
  private final boolean ambientOcclusion;
  private final Transformation root;

  private StandaloneGeometryBakingContext(IGeometryBakingContext parent, boolean gui3d, boolean blockLight, boolean ambientOcclusion, Transformation root) {
    this.parent = parent;
    this.gui3d = gui3d;
    this.blockLight = blockLight;
    this.ambientOcclusion = ambientOcclusion;
    this.root = root;
  }

  /** Starts a builder copying the values of the given context */
  public static Builder builder(IGeometryBakingContext context) {
    return new Builder(context);
  }

  @Override public String getModelName() { return parent.getModelName(); }
  @Override public boolean hasMaterial(String name) { return parent.hasMaterial(name); }
  @Override public Material getMaterial(String name) { return parent.getMaterial(name); }
  @Override public boolean isGui3d() { return gui3d; }
  @Override public boolean useBlockLight() { return blockLight; }
  @Override public boolean useAmbientOcclusion() { return ambientOcclusion; }
  @Override public ItemTransforms getTransforms() { return parent.getTransforms(); }
  @Override public Transformation getRootTransform() { return root; }
  @Nullable @Override public ResourceLocation getRenderTypeHint() { return parent.getRenderTypeHint(); }
  @Override public boolean isComponentVisible(String component, boolean fallback) { return parent.isComponentVisible(component, fallback); }

  /** Builder for overriding fields */
  public static class Builder {
    private final IGeometryBakingContext parent;
    private boolean gui3d;
    private boolean blockLight;
    private boolean ambientOcclusion;
    private Transformation root;

    private Builder(IGeometryBakingContext parent) {
      this.parent = parent;
      this.gui3d = parent.isGui3d();
      this.blockLight = parent.useBlockLight();
      this.ambientOcclusion = parent.useAmbientOcclusion();
      this.root = parent.getRootTransform();
    }

    public Builder withGui3d(boolean gui3d) { this.gui3d = gui3d; return this; }
    public Builder withUseBlockLight(boolean blockLight) { this.blockLight = blockLight; return this; }
    public Builder withUseAmbientOcclusion(boolean ambientOcclusion) { this.ambientOcclusion = ambientOcclusion; return this; }
    public Builder withRootTransform(Transformation root) { this.root = root; return this; }

    public StandaloneGeometryBakingContext build() {
      return new StandaloneGeometryBakingContext(parent, gui3d, blockLight, ambientOcclusion, root);
    }
  }
}
