package slimeknights.mantle.platform.client.model;

import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Function;

/** Custom geometry that can be baked, replacing NeoForge's {@code IUnbakedGeometry}. */
public interface IUnbakedGeometry<T extends IUnbakedGeometry<T>> {
  /** Bakes the geometry into a model */
  BakedModel bake(IGeometryBakingContext context, ModelBaker baker, Function<Material,TextureAtlasSprite> spriteGetter, ModelState state, ItemOverrides overrides);

  /** Resolves any parents needed before baking */
  default void resolveParents(Function<ResourceLocation,UnbakedModel> modelGetter, IGeometryBakingContext context) {}
}
