package slimeknights.mantle.platform.client.model;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.SimpleBakedModel;

import java.util.Collection;

/** Simplified replacement for NeoForge's {@code CompositeModel}: all quads end up in a single unculled list, render type hints are ignored. */
public final class CompositeModel {
  private CompositeModel() {}

  /** Baked form */
  public static final class Baked {
    private Baked() {}

    public static Builder builder(IGeometryBakingContext context, TextureAtlasSprite particle, ItemOverrides overrides, ItemTransforms transforms) {
      return new Builder(new SimpleBakedModel.Builder(context.useAmbientOcclusion(), context.useBlockLight(), context.isGui3d(), transforms, overrides).particle(particle));
    }

    /** Builder collecting quads */
    public static final class Builder {
      private final SimpleBakedModel.Builder builder;

      private Builder(SimpleBakedModel.Builder builder) {
        this.builder = builder;
      }

      public Builder addQuads(RenderTypeGroup group, Collection<BakedQuad> quads) {
        for (BakedQuad quad : quads) {
          builder.addUnculledFace(quad);
        }
        return this;
      }

      public Builder addQuads(RenderTypeGroup group, BakedQuad... quads) {
        for (BakedQuad quad : quads) {
          builder.addUnculledFace(quad);
        }
        return this;
      }

      public BakedModel build() {
        return builder.build();
      }
    }
  }
}
