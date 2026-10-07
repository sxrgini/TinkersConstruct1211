package slimeknights.mantle.platform.client.model;

import com.google.gson.Gson;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.math.Transformation;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelResolver;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransform;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.util.GsonHelper;
import org.joml.Vector3f;
import slimeknights.mantle.Mantle;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.Reader;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

/**
 * Makes the NeoForge style {@code "loader"} key in model JSON work on Fabric. Models in non-vanilla namespaces are checked for the key,
 * and when it names a registered {@link IGeometryLoader} the model is wrapped in a {@link GeometryModel}.
 */
public final class GeometryLoaders {
  private static final Map<ResourceLocation,IGeometryLoader<?>> LOADERS = new HashMap<>();
  private static final Gson GSON = BlockModel.GSON;
  private static final JsonDeserializationContext CONTEXT = new JsonDeserializationContext() {
    @Override
    public <T> T deserialize(JsonElement json, java.lang.reflect.Type type) {
      return GSON.fromJson(json, type);
    }
  };
  private static boolean initialized = false;

  private GeometryLoaders() {}

  /** Registers a geometry loader under the given ID, call during client initialization */
  public static void register(ResourceLocation id, IGeometryLoader<?> loader) {
    if (LOADERS.putIfAbsent(id, loader) != null) {
      throw new IllegalArgumentException("Duplicate geometry loader " + id);
    }
    if (!initialized) {
      initialized = true;
      ModelLoadingPlugin.register(context -> context.resolveModel().register(GeometryLoaders::resolve));
    }
  }

  @Nullable
  private static UnbakedModel resolve(ModelResolver.Context context) {
    ResourceLocation id = context.id();
    // skip blockstate variants and vanilla models
    if (id.getNamespace().equals("minecraft")) {
      return null;
    }
    Optional<Resource> resource = Minecraft.getInstance().getResourceManager().getResource(id.withPrefix("models/").withSuffix(".json"));
    if (resource.isEmpty()) {
      return null;
    }
    JsonObject json;
    try (Reader reader = resource.get().openAsReader()) {
      JsonElement element = GsonHelper.parse(reader);
      if (!element.isJsonObject()) {
        return null;
      }
      json = element.getAsJsonObject();
    } catch (IOException | RuntimeException e) {
      return null;
    }
    if (!json.has("loader")) {
      return null;
    }
    ResourceLocation loaderId = ResourceLocation.parse(GsonHelper.getAsString(json, "loader"));
    IGeometryLoader<?> loader = LOADERS.get(loaderId);
    if (loader == null) {
      return null;
    }
    try {
      BlockModel base = BlockModel.fromString(json.toString());
      base.name = id.toString();
      IUnbakedGeometry<?> geometry = loader.read(json, CONTEXT);
      Transformation root = json.has("transform") ? readTransform(json.get("transform")) : Transformation.identity();
      ResourceLocation hint = json.has("render_type") ? ResourceLocation.parse(GsonHelper.getAsString(json, "render_type")) : null;
      return new GeometryModel(base, geometry, new BlockGeometryBakingContext(base, id.toString(), root, hint));
    } catch (RuntimeException e) {
      Mantle.logger.error("Failed to load geometry model {}", id, e);
      return null;
    }
  }

  /** Reads the root transform in the same format as item transforms: translation (in 1/16 blocks), rotation (degrees) and scale */
  private static Transformation readTransform(JsonElement element) {
    ItemTransform transform = GSON.fromJson(element, ItemTransform.class);
    Vector3f rotation = transform.rotation;
    return new Transformation(
      new Vector3f(transform.translation).mul(1f / 16f),
      new org.joml.Quaternionf().rotationXYZ((float) Math.toRadians(rotation.x()), (float) Math.toRadians(rotation.y()), (float) Math.toRadians(rotation.z())),
      new Vector3f(transform.scale),
      null);
  }

  /** Unbaked model combining a vanilla block model (parents, textures, display) with custom geometry */
  public static final class GeometryModel implements UnbakedModel {
    private final BlockModel base;
    private final IUnbakedGeometry<?> geometry;
    private final BlockGeometryBakingContext context;

    private GeometryModel(BlockModel base, IUnbakedGeometry<?> geometry, BlockGeometryBakingContext context) {
      this.base = base;
      this.geometry = geometry;
      this.context = context;
    }

    @Override
    public Collection<ResourceLocation> getDependencies() {
      return base.getDependencies();
    }

    @Override
    public void resolveParents(Function<ResourceLocation,UnbakedModel> getter) {
      base.resolveParents(getter);
      geometry.resolveParents(getter, context);
    }

    @Nullable
    @Override
    public BakedModel bake(ModelBaker baker, Function<Material,TextureAtlasSprite> spriteGetter, ModelState state) {
      return geometry.bake(context, baker, spriteGetter, state, new ItemOverrides(baker, base, base.getOverrides()));
    }
  }
}
