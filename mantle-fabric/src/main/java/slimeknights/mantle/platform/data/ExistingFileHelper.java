package slimeknights.mantle.platform.data;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.IoSupplier;
import net.minecraft.server.packs.resources.Resource;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Replacement for NeoForge's {@code ExistingFileHelper} used by data providers to check for and read resources that already exist.
 * Looks up files on the classpath (which includes the vanilla assets and the mod resources in a development environment)
 * and in extra root folders passed through the {@code mantle.datagen.existing} system property, separated by the path separator.
 */
public class ExistingFileHelper {
  /** System property naming extra resource roots */
  public static final String EXTRA_ROOTS_PROPERTY = "mantle.datagen.existing";

  private final List<Path> extraRoots = new ArrayList<>();
  private final Set<String> generated = new HashSet<>();

  public ExistingFileHelper() {
    String roots = System.getProperty(EXTRA_ROOTS_PROPERTY);
    if (roots != null && !roots.isEmpty()) {
      for (String root : roots.split(java.io.File.pathSeparator)) {
        extraRoots.add(Path.of(root));
      }
    }
  }

  /** Creates a helper looking in the given extra roots */
  public ExistingFileHelper(List<Path> roots) {
    this();
    extraRoots.addAll(roots);
  }

  /** Type of resource, used to build the path */
  public interface IResourceType {
    PackType getPackType();
    String getSuffix();
    String getPrefix();
  }

  /** Simple resource type implementation */
  public record ResourceType(PackType packType, String suffix, String prefix) implements IResourceType {
    @Override public PackType getPackType() { return packType; }
    @Override public String getSuffix() { return suffix; }
    @Override public String getPrefix() { return prefix; }
  }

  private static String path(ResourceLocation loc, PackType type, String extension, String pathSuffix) {
    String root = type == PackType.CLIENT_RESOURCES ? "assets" : "data";
    return root + "/" + loc.getNamespace() + "/" + (pathSuffix.isEmpty() ? "" : pathSuffix + "/") + loc.getPath() + extension;
  }

  /** Marks a resource as generated, so later providers can reference it */
  public void trackGenerated(ResourceLocation loc, PackType type, String extension, String pathSuffix) {
    generated.add(path(loc, type, extension, pathSuffix));
  }

  public void trackGenerated(ResourceLocation loc, IResourceType type) {
    trackGenerated(loc, type.getPackType(), type.getSuffix(), type.getPrefix());
  }

  /** Checks if the resource exists */
  public boolean exists(ResourceLocation loc, PackType type, String extension, String pathSuffix) {
    String path = path(loc, type, extension, pathSuffix);
    if (generated.contains(path)) {
      return true;
    }
    if (Thread.currentThread().getContextClassLoader().getResource(path) != null || ExistingFileHelper.class.getClassLoader().getResource(path) != null) {
      return true;
    }
    for (Path root : extraRoots) {
      if (Files.exists(root.resolve(path))) {
        return true;
      }
    }
    return false;
  }

  public boolean exists(ResourceLocation loc, IResourceType type) {
    return exists(loc, type.getPackType(), type.getSuffix(), type.getPrefix());
  }

  /** Opens the resource, throwing if it does not exist */
  public Resource getResource(ResourceLocation loc, PackType type, String extension, String pathSuffix) throws IOException {
    String path = path(loc, type, extension, pathSuffix);
    for (Path root : extraRoots) {
      Path file = root.resolve(path);
      if (Files.exists(file)) {
        return new Resource(null, () -> Files.newInputStream(file));
      }
    }
    URL url = Thread.currentThread().getContextClassLoader().getResource(path);
    if (url == null) {
      url = ExistingFileHelper.class.getClassLoader().getResource(path);
    }
    if (url == null) {
      throw new FileNotFoundException(path);
    }
    URL finalUrl = url;
    return new Resource(null, (IoSupplier<InputStream>) finalUrl::openStream);
  }

  public Resource getResource(ResourceLocation loc, IResourceType type) throws IOException {
    return getResource(loc, type.getPackType(), type.getSuffix(), type.getPrefix());
  }

  /** Always enabled, data generation always has access to resources */
  public boolean isEnabled() {
    return true;
  }
}
