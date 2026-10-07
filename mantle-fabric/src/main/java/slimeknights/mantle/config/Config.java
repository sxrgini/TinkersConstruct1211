package slimeknights.mantle.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.fabricmc.loader.api.FabricLoader;
import org.jetbrains.annotations.ApiStatus.Internal;
import slimeknights.mantle.Mantle;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;

/**
 * Base class for all Mantle specific config options. Fabric has no config system, so this is a small JSON file at {@code config/mantle.json}.
 */
@Internal
public class Config {
  public enum HeartRenderer {
    DISABLE, NO_MAX, WITH_MAX
  }

  /** Simple config value holder, mirrors the {@code get()} API of NeoForge's config values */
  public static class Value<T> {
    private final String name;
    private final T defaultValue;
    private final Function<JsonElement,T> reader;
    private T value;

    private Value(String name, T defaultValue, Function<JsonElement,T> reader) {
      this.name = name;
      this.defaultValue = defaultValue;
      this.reader = reader;
      this.value = defaultValue;
    }

    public T get() {
      return value;
    }

    private void load(JsonObject json) {
      if (json.has(name)) {
        try {
          value = reader.apply(json.get(name));
          return;
        } catch (RuntimeException e) {
          Mantle.logger.warn("Invalid value for config option {}, using default", name);
        }
      }
      value = defaultValue;
    }

    private JsonElement save() {
      if (value instanceof Enum<?> e) {
        return GSON.toJsonTree(e.name());
      }
      return GSON.toJsonTree(value);
    }
  }

  private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
  private static final List<Value<?>> VALUES = new ArrayList<>();

  private static <T> Value<T> register(Value<T> value) {
    VALUES.add(value);
    return value;
  }

  /** Heart renderer mode. DISABLE uses the vanilla heart renderer, WITH_MAX shows max health in colored containers behind the bar. */
  public static final Value<HeartRenderer> HEART_RENDERER = register(new Value<>("heartRenderer", HeartRenderer.WITH_MAX,
    json -> HeartRenderer.valueOf(json.getAsString().toUpperCase(Locale.ROOT))));

  /** If true, enables the fluid fog fix. If false, disables it for better shader compatability. */
  public static final Value<Boolean> ENABLE_FLUID_FOG_FIX = register(new Value<>("enableFluidFogFix", true, JsonElement::getAsBoolean));

  /** If true, the fallback shader for fluid uses a text shader, which provides better compatability. */
  public static final Value<Boolean> FLUID_USE_TEXT_SHADER = register(new Value<>("fluidFallbackUseTextShader", true, JsonElement::getAsBoolean));

  /** List of preferences for tag outputs */
  private static final List<String> DEFAULT_TAG_PREFERENCES = Arrays.asList("minecraft", "tconstruct", "tmechworks", "metalborn", "embers", "create", "immersiveengineering", "mekanism", "thermal");
  public static final Value<List<? extends String>> TAG_PREFERENCES = register(new Value<List<? extends String>>("tagPreferences", DEFAULT_TAG_PREFERENCES, json -> {
    List<String> list = new ArrayList<>();
    json.getAsJsonArray().forEach(e -> list.add(e.getAsString()));
    return list;
  }));

  private Config() {}

  /** Loads the config from disk, writing the defaults back so new options show up */
  public static void load() {
    Path path = FabricLoader.getInstance().getConfigDir().resolve("mantle.json");
    JsonObject json = new JsonObject();
    if (Files.exists(path)) {
      try (Reader reader = Files.newBufferedReader(path)) {
        JsonElement element = GSON.fromJson(reader, JsonElement.class);
        if (element != null && element.isJsonObject()) {
          json = element.getAsJsonObject();
        }
      } catch (IOException | RuntimeException e) {
        Mantle.logger.error("Failed to read Mantle config, using defaults", e);
      }
    }
    JsonObject output = new JsonObject();
    for (Value<?> value : VALUES) {
      value.load(json);
      output.add(value.name, value.save());
    }
    try (Writer writer = Files.newBufferedWriter(path)) {
      GSON.toJson(output, writer);
    } catch (IOException e) {
      Mantle.logger.error("Failed to write Mantle config", e);
    }
  }
}
