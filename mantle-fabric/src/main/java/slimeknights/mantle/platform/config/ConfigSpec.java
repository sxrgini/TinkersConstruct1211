package slimeknights.mantle.platform.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
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
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Forge style config builder backed by a JSON file, replacing Forge's {@code ForgeConfigSpec}.
 * Groups made with {@link Builder#push} become nested JSON objects. Comments and translation keys are kept for API compatibility but not written.
 */
public class ConfigSpec {
  /** Type of config, decides the file name */
  public enum Type {
    COMMON, CLIENT, SERVER
  }

  private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
  private final List<ConfigValue<?>> values;

  private ConfigSpec(List<ConfigValue<?>> values) {
    this.values = values;
  }

  /** Loads the spec from {@code config/<modId>-<type>.json}, writing back defaults so new options appear. Client configs are skipped on a dedicated server. */
  public void register(String modId, Type type) {
    if (type == Type.CLIENT && FabricLoader.getInstance().getEnvironmentType() != EnvType.CLIENT) {
      return;
    }
    Path path = FabricLoader.getInstance().getConfigDir().resolve(modId + "-" + type.name().toLowerCase(Locale.ROOT) + ".json");
    JsonObject root = new JsonObject();
    if (Files.exists(path)) {
      try (Reader reader = Files.newBufferedReader(path)) {
        JsonElement element = GSON.fromJson(reader, JsonElement.class);
        if (element != null && element.isJsonObject()) {
          root = element.getAsJsonObject();
        }
      } catch (IOException | RuntimeException e) {
        Mantle.logger.error("Failed to read config {}, using defaults", path, e);
      }
    }
    JsonObject output = new JsonObject();
    for (ConfigValue<?> value : values) {
      value.load(root);
      value.save(output);
    }
    try (Writer writer = Files.newBufferedWriter(path)) {
      GSON.toJson(output, writer);
    } catch (IOException e) {
      Mantle.logger.error("Failed to write config {}", path, e);
    }
  }

  /** Base value */
  public static class ConfigValue<T> implements Supplier<T> {
    protected final List<String> path;
    protected final T defaultValue;
    protected final Function<JsonElement,T> reader;
    protected final Predicate<T> validator;
    protected T value;

    protected ConfigValue(List<String> path, T defaultValue, Function<JsonElement,T> reader, Predicate<T> validator) {
      this.path = List.copyOf(path);
      this.defaultValue = defaultValue;
      this.reader = reader;
      this.validator = validator;
      this.value = defaultValue;
    }

    @Override
    public T get() {
      return value;
    }

    public void set(T value) {
      this.value = value;
    }

    public T getDefault() {
      return defaultValue;
    }

    public List<String> getPath() {
      return path;
    }

    private void load(JsonObject root) {
      JsonElement element = root;
      for (String segment : path) {
        element = element != null && element.isJsonObject() ? element.getAsJsonObject().get(segment) : null;
      }
      value = defaultValue;
      if (element != null) {
        try {
          T read = reader.apply(element);
          if (validator.test(read)) {
            value = read;
          } else {
            Mantle.logger.warn("Config value {} is out of range, using the default", String.join(".", path));
          }
        } catch (RuntimeException e) {
          Mantle.logger.warn("Invalid config value {}, using the default", String.join(".", path));
        }
      }
    }

    protected JsonElement serialize(T value) {
      return GSON.toJsonTree(value);
    }

    private void save(JsonObject output) {
      JsonObject current = output;
      for (int i = 0; i < path.size() - 1; i++) {
        JsonElement next = current.get(path.get(i));
        if (next == null || !next.isJsonObject()) {
          next = new JsonObject();
          current.add(path.get(i), next);
        }
        current = next.getAsJsonObject();
      }
      current.add(path.get(path.size() - 1), serialize(value));
    }
  }

  public static class BooleanValue extends ConfigValue<Boolean> {
    private BooleanValue(List<String> path, boolean def) {
      super(path, def, JsonElement::getAsBoolean, v -> true);
    }
  }

  public static class IntValue extends ConfigValue<Integer> {
    private IntValue(List<String> path, int def, int min, int max) {
      super(path, def, JsonElement::getAsInt, v -> v >= min && v <= max);
    }
  }

  public static class LongValue extends ConfigValue<Long> {
    private LongValue(List<String> path, long def, long min, long max) {
      super(path, def, JsonElement::getAsLong, v -> v >= min && v <= max);
    }
  }

  public static class DoubleValue extends ConfigValue<Double> {
    private DoubleValue(List<String> path, double def, double min, double max) {
      super(path, def, JsonElement::getAsDouble, v -> v >= min && v <= max);
    }
  }

  public static class EnumValue<E extends Enum<E>> extends ConfigValue<E> {
    private EnumValue(List<String> path, E def) {
      super(path, def, e -> Enum.valueOf(def.getDeclaringClass(), e.getAsString().toUpperCase(Locale.ROOT)), v -> true);
    }

    @Override
    protected JsonElement serialize(E value) {
      return GSON.toJsonTree(value.name());
    }
  }

  /** Builder, mirrors the Forge builder methods */
  public static class Builder {
    private final List<String> currentPath = new ArrayList<>();
    private final List<ConfigValue<?>> values = new ArrayList<>();

    public Builder comment(String comment) {
      return this;
    }

    public Builder comment(String... comment) {
      return this;
    }

    public Builder translation(String key) {
      return this;
    }

    public Builder worldRestart() {
      return this;
    }

    public Builder push(String path) {
      currentPath.addAll(Arrays.asList(path.split("\\.")));
      return this;
    }

    public Builder push(List<String> path) {
      currentPath.addAll(path);
      return this;
    }

    public Builder pop() {
      return pop(1);
    }

    public Builder pop(int count) {
      for (int i = 0; i < count; i++) {
        currentPath.remove(currentPath.size() - 1);
      }
      return this;
    }

    private List<String> path(String name) {
      List<String> path = new ArrayList<>(currentPath);
      path.addAll(Arrays.asList(name.split("\\.")));
      return path;
    }

    private <V extends ConfigValue<?>> V add(V value) {
      values.add(value);
      return value;
    }

    public BooleanValue define(String name, boolean def) {
      return add(new BooleanValue(path(name), def));
    }

    public IntValue defineInRange(String name, int def, int min, int max) {
      return add(new IntValue(path(name), def, min, max));
    }

    public LongValue defineInRange(String name, long def, long min, long max) {
      return add(new LongValue(path(name), def, min, max));
    }

    public DoubleValue defineInRange(String name, double def, double min, double max) {
      return add(new DoubleValue(path(name), def, min, max));
    }

    public <E extends Enum<E>> EnumValue<E> defineEnum(String name, E def) {
      return add(new EnumValue<>(path(name), def));
    }

    public ConfigValue<String> define(String name, String def) {
      return add(new ConfigValue<>(path(name), def, JsonElement::getAsString, v -> true));
    }

    public ConfigValue<List<? extends String>> defineList(String name, List<? extends String> def, Predicate<Object> elementValidator) {
      return add(new ConfigValue<List<? extends String>>(path(name), def, element -> {
        JsonArray array = element.getAsJsonArray();
        List<String> list = new ArrayList<>(array.size());
        array.forEach(e -> list.add(e.getAsString()));
        return list;
      }, v -> v.stream().allMatch(elementValidator)));
    }

    public ConfigSpec build() {
      return new ConfigSpec(List.copyOf(values));
    }
  }
}
