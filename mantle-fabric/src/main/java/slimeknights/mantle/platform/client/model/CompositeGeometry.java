package slimeknights.mantle.platform.client.model;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.RandomSource;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/** Replacement for the {@code forge:composite} loader: bakes each child model and merges the quads into one model, render types are ignored. */
public record CompositeGeometry(List<BlockModel> children) implements IUnbakedGeometry<CompositeGeometry> {
  public static final IGeometryLoader<CompositeGeometry> LOADER = (json, context) -> read(json);

  private static CompositeGeometry read(JsonObject json) {
    List<BlockModel> children = new ArrayList<>();
    for (Map.Entry<String,com.google.gson.JsonElement> entry : GsonHelper.getAsJsonObject(json, "children").entrySet()) {
      BlockModel child = BlockModel.fromString(entry.getValue().toString());
      child.name = entry.getKey();
      children.add(child);
    }
    return new CompositeGeometry(children);
  }

  @Override
  public void resolveParents(Function<ResourceLocation,UnbakedModel> modelGetter, IGeometryBakingContext context) {
    for (BlockModel child : children) {
      child.resolveParents(modelGetter);
    }
  }

  @Override
  public BakedModel bake(IGeometryBakingContext context, ModelBaker baker, Function<Material,TextureAtlasSprite> spriteGetter, ModelState state, ItemOverrides overrides) {
    TextureAtlasSprite particle = spriteGetter.apply(context.getMaterial("particle"));
    CompositeModel.Baked.Builder builder = CompositeModel.Baked.builder(context, particle, overrides, context.getTransforms());
    RandomSource random = RandomSource.create();
    for (BlockModel child : children) {
      BakedModel baked = child.bake(baker, spriteGetter, state);
      if (baked == null) continue;
      builder.addQuads(RenderTypeGroup.EMPTY, baked.getQuads(null, null, random));
      for (Direction direction : Direction.values()) {
        builder.addQuads(RenderTypeGroup.EMPTY, baked.getQuads(null, direction, random));
      }
    }
    return builder.build();
  }
}
