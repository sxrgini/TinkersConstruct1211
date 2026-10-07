package slimeknights.mantle.platform.client.model;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.SimpleBakedModel;
import net.minecraft.core.Direction;

/** Simple builder for baked models, equivalent of Forge's IModelBuilder. Render types are not applied on Fabric. */
public interface IModelBuilder<T extends IModelBuilder<T>> {
  /** Adds a quad that is culled when the neighbor in the given direction is solid */
  void addCulledFace(Direction facing, BakedQuad quad);

  /** Adds a quad that is never culled */
  void addUnculledFace(BakedQuad quad);

  /** Builds the final model */
  BakedModel build();

  /** Creates a builder wrapping the vanilla simple baked model */
  static IModelBuilder<?> of(boolean ambientOcclusion, boolean blockLight, boolean gui3d, ItemTransforms transforms, ItemOverrides overrides, TextureAtlasSprite particle, RenderTypeGroup renderTypes) {
    return new Simple(new SimpleBakedModel.Builder(ambientOcclusion, blockLight, gui3d, transforms, overrides).particle(particle));
  }

  /** Implementation backed by the vanilla builder */
  record Simple(SimpleBakedModel.Builder builder) implements IModelBuilder<Simple> {
    @Override
    public void addCulledFace(Direction facing, BakedQuad quad) {
      builder.addCulledFace(facing, quad);
    }

    @Override
    public void addUnculledFace(BakedQuad quad) {
      builder.addUnculledFace(quad);
    }

    @Override
    public BakedModel build() {
      return builder.build();
    }
  }
}
