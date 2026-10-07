package slimeknights.mantle.platform.loot;

import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import slimeknights.mantle.Mantle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Loads and applies global loot modifiers from {@code data/<namespace>/loot_modifiers/*.json}, replacing NeoForge's system.
 * Modifiers are applied by {@link slimeknights.mantle.mixin.LootTableMixin}.
 */
public class GlobalLootModifierManager extends SimpleJsonResourceReloadListener implements IdentifiableResourceReloadListener {
  public static final GlobalLootModifierManager INSTANCE = new GlobalLootModifierManager();
  private static final ResourceLocation ID = Mantle.getResource("loot_modifiers");

  private static final Map<ResourceLocation,MapCodec<? extends IGlobalLootModifier>> BY_ID = new HashMap<>();
  private static final Map<MapCodec<? extends IGlobalLootModifier>,ResourceLocation> BY_CODEC = new HashMap<>();

  static final Codec<MapCodec<? extends IGlobalLootModifier>> TYPE_CODEC = ResourceLocation.CODEC.flatXmap(
    id -> {
      MapCodec<? extends IGlobalLootModifier> codec = BY_ID.get(id);
      return codec != null ? DataResult.success(codec) : DataResult.error(() -> "Unknown loot modifier type " + id);
    },
    codec -> {
      ResourceLocation id = BY_CODEC.get(codec);
      return id != null ? DataResult.success(id) : DataResult.error(() -> "Unregistered loot modifier codec " + codec);
    });

  private List<IGlobalLootModifier> modifiers = List.of();

  private GlobalLootModifierManager() {
    super(com.google.gson.Gson.class.cast(new com.google.gson.Gson()), "loot_modifiers");
  }

  /** Registers a loot modifier type */
  public static void register(ResourceLocation id, MapCodec<? extends IGlobalLootModifier> codec) {
    if (BY_ID.putIfAbsent(id, codec) != null) {
      throw new IllegalArgumentException("Duplicate loot modifier type " + id);
    }
    BY_CODEC.put(codec, id);
  }

  /** Registers the reload listener, call during mod initialization */
  public static void init() {
    ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(INSTANCE);
  }

  @Override
  public ResourceLocation getFabricId() {
    return ID;
  }

  @Override
  protected void apply(Map<ResourceLocation,JsonElement> files, ResourceManager manager, ProfilerFiller profiler) {
    List<IGlobalLootModifier> loaded = new ArrayList<>();
    files.forEach((id, json) -> {
      // empty objects allow datapacks to remove modifiers
      if (json.isJsonObject() && json.getAsJsonObject().size() == 0) {
        return;
      }
      IGlobalLootModifier.DIRECT_CODEC.parse(JsonOps.INSTANCE, json)
        .ifSuccess(loaded::add)
        .ifError(error -> Mantle.logger.error("Failed to load loot modifier {}: {}", id, error.message()));
    });
    this.modifiers = List.copyOf(loaded);
    Mantle.logger.info("Loaded {} global loot modifiers", loaded.size());
  }

  /** Gets all loaded modifiers */
  public List<IGlobalLootModifier> getModifiers() {
    return modifiers;
  }

  /** Applies all modifiers to the given loot */
  public ObjectArrayList<ItemStack> modifyLoot(ObjectArrayList<ItemStack> loot, LootContext context) {
    for (IGlobalLootModifier modifier : modifiers) {
      loot = modifier.apply(loot, context);
    }
    return loot;
  }
}
