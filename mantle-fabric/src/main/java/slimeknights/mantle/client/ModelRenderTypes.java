package slimeknights.mantle.client;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.block.Block;
import slimeknights.mantle.Mantle;

import javax.annotation.Nullable;
import java.io.Reader;
import java.util.Set;

/**
 * Forge models can declare a {@code render_type} that decides which layer the block renders in. Fabric ignores that key, so this reads it from the block's model
 * and registers the matching layer. Only applies to blocks from the listed namespaces.
 */
@Environment(EnvType.CLIENT)
public final class ModelRenderTypes implements SimpleSynchronousResourceReloadListener {
  private static final ResourceLocation ID = Mantle.getResource("model_render_types");
  private static final Set<String> NAMESPACES = Set.of("mantle", "tconstruct");

  private ModelRenderTypes() {}

  public static void init() {
    ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(new ModelRenderTypes());
  }

  @Override
  public ResourceLocation getFabricId() {
    return ID;
  }

  @Override
  public void onResourceManagerReload(ResourceManager manager) {
    for (Block block : BuiltInRegistries.BLOCK) {
      ResourceLocation key = BuiltInRegistries.BLOCK.getKey(block);
      if (!NAMESPACES.contains(key.getNamespace())) {
        continue;
      }
      try {
        JsonObject state = read(manager, key.withPrefix("blockstates/").withSuffix(".json"));
        ResourceLocation model = state == null ? null : findModel(state);
        String type = model == null ? null : findRenderType(manager, model);
        if (type != null) {
          switch (type) {
            case "minecraft:translucent" -> BlockRenderLayerMap.INSTANCE.putBlock(block, RenderType.translucent());
            case "minecraft:cutout" -> BlockRenderLayerMap.INSTANCE.putBlock(block, RenderType.cutout());
            case "minecraft:cutout_mipped" -> BlockRenderLayerMap.INSTANCE.putBlock(block, RenderType.cutoutMipped());
            default -> {}
          }
        }
      } catch (RuntimeException e) {
        Mantle.logger.debug("Failed to determine render type of {}", key, e);
      }
    }
  }

  @Nullable
  private static JsonObject read(ResourceManager manager, ResourceLocation path) {
    try (Reader reader = manager.getResource(path).map(r -> {
      try { return r.openAsReader(); } catch (java.io.IOException e) { return null; }
    }).orElse(null)) {
      if (reader == null) {
        return null;
      }
      JsonElement element = GsonHelper.parse(reader);
      return element.isJsonObject() ? element.getAsJsonObject() : null;
    } catch (java.io.IOException | RuntimeException e) {
      return null;
    }
  }

  /** Finds the first model referenced by the blockstate */
  @Nullable
  private static ResourceLocation findModel(JsonElement element) {
    if (element.isJsonObject()) {
      JsonObject object = element.getAsJsonObject();
      if (object.has("model") && object.get("model").isJsonPrimitive()) {
        return ResourceLocation.tryParse(object.get("model").getAsString());
      }
      for (String key : new String[] {"variants", "multipart", "apply"}) {
        if (object.has(key)) {
          ResourceLocation found = findModel(object.get(key));
          if (found != null) {
            return found;
          }
        }
      }
      for (var entry : object.entrySet()) {
        if (entry.getValue().isJsonObject() || entry.getValue().isJsonArray()) {
          ResourceLocation found = findModel(entry.getValue());
          if (found != null) {
            return found;
          }
        }
      }
    } else if (element.isJsonArray()) {
      for (JsonElement child : element.getAsJsonArray()) {
        ResourceLocation found = findModel(child);
        if (found != null) {
          return found;
        }
      }
    }
    return null;
  }

  /** Finds the render type of the model or its closest parent that sets one */
  @Nullable
  private static String findRenderType(ResourceManager manager, ResourceLocation model) {
    for (int depth = 0; depth < 16 && model != null; depth++) {
      JsonObject json = read(manager, model.withPrefix("models/").withSuffix(".json"));
      if (json == null) {
        return null;
      }
      if (json.has("render_type")) {
        return GsonHelper.getAsString(json, "render_type");
      }
      model = json.has("parent") ? ResourceLocation.tryParse(GsonHelper.getAsString(json, "parent")) : null;
    }
    return null;
  }
}
