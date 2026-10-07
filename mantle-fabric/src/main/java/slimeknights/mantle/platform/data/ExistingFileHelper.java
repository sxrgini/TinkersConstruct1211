package slimeknights.mantle.platform.data;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.Resource;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;

/**
 * Datagen helper to check or read resources that exist in loaded mods or were generated earlier in the run, replacing NeoForge's {@code ExistingFileHelper}.
 */
public final class ExistingFileHelper {
  /** Shared instance for datagen runs */
  public static final ExistingFileHelper INSTANCE = new ExistingFileHelper();

  private final Set<String> generated = new HashSet<>();

  private ExistingFileHelper() {}

  /** Type of resource: pack type, file extension, and the folder the file is in */
  public record ResourceType(PackType packType, String suffix, String prefix) {}

  private static String key(ResourceLocation location, ResourceType type) {
    return type.packType().name() + ":" + location.getNamespace() + ":" + type.prefix() + "/" + location.getPath() + type.suffix();
  }

  /** Marks the resource as generated, so {@link #exists} returns true for it */
  public void trackGenerated(ResourceLocation location, ResourceType type) {
    generated.add(key(location, type));
  }

  /** Checks if the resource exists in a loaded mod or was generated */
  public boolean exists(ResourceLocation location, ResourceType type) {
    return generated.contains(key(location, type)) || find(location, type.packType(), type.suffix(), type.prefix()).isPresent();
  }

  /** Checks if the resource exists in a loaded mod or was generated */
  public boolean exists(ResourceLocation location, PackType packType, String suffix, String prefix) {
    return exists(location, new ResourceType(packType, suffix, prefix));
  }

  /** Opens a resource from a loaded mod */
  public Resource getResource(ResourceLocation location, PackType packType, String suffix, String prefix) {
    Path path = find(location, packType, suffix, prefix).orElseThrow(() -> new NoSuchElementException("Resource not found: " + location + suffix));
    return new Resource(null, () -> {
      InputStream stream = Files.newInputStream(path);
      return stream;
    });
  }

  private static Optional<Path> find(ResourceLocation location, PackType packType, String suffix, String prefix) {
    String relative = packType.getDirectory() + "/" + location.getNamespace() + "/" + (prefix.isEmpty() ? "" : prefix + "/") + location.getPath() + suffix;
    for (ModContainer mod : FabricLoader.getInstance().getAllMods()) {
      Optional<Path> path = mod.findPath(relative);
      if (path.isPresent()) {
        return path;
      }
    }
    return Optional.empty();
  }
}
