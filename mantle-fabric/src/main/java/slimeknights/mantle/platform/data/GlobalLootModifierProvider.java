package slimeknights.mantle.platform.data;

import com.google.gson.JsonElement;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import com.mojang.serialization.JsonOps;
import slimeknights.mantle.platform.loot.IGlobalLootModifier;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/** Data provider for global loot modifiers, replacing NeoForge's provider of the same name. Writes {@code data/<modid>/loot_modifiers/<name>.json}. */
public abstract class GlobalLootModifierProvider implements DataProvider {
  private final PackOutput.PathProvider pathProvider;
  private final String modId;
  private final CompletableFuture<HolderLookup.Provider> registries;
  private final Map<String,IGlobalLootModifier> modifiers = new LinkedHashMap<>();

  protected GlobalLootModifierProvider(PackOutput output, String modId, CompletableFuture<HolderLookup.Provider> registries) {
    this.pathProvider = output.createPathProvider(PackOutput.Target.DATA_PACK, "loot_modifiers");
    this.modId = modId;
    this.registries = registries;
  }

  /** Adds all modifiers using {@link #add(String, IGlobalLootModifier)} */
  protected abstract void start(HolderLookup.Provider lookup);

  /** Adds a modifier with the given name */
  protected void add(String name, IGlobalLootModifier modifier) {
    if (modifiers.put(name, modifier) != null) {
      throw new IllegalStateException("Duplicate loot modifier " + name);
    }
  }

  @Override
  public CompletableFuture<?> run(CachedOutput output) {
    return registries.thenCompose(lookup -> {
      start(lookup);
      RegistryOps<JsonElement> ops = lookup.createSerializationContext(JsonOps.INSTANCE);
      return CompletableFuture.allOf(modifiers.entrySet().stream().map(entry -> {
        JsonElement json = IGlobalLootModifier.DIRECT_CODEC.encodeStart(ops, entry.getValue()).getOrThrow(IllegalStateException::new);
        return DataProvider.saveStable(output, json, pathProvider.json(ResourceLocation.fromNamespaceAndPath(modId, entry.getKey())));
      }).toArray(CompletableFuture[]::new));
    });
  }

  @Override
  public String getName() {
    return "Global Loot Modifiers : " + modId;
  }
}
