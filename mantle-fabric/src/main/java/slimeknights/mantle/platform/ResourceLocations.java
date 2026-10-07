package slimeknights.mantle.platform;

import net.minecraft.resources.ResourceLocation;

/** Replacement for NeoForge's {@code ResourceLocation#compareNamespaced} */
public final class ResourceLocations {
  private ResourceLocations() {}

  /** Compares by namespace first, then by path */
  public static int compareNamespaced(ResourceLocation a, ResourceLocation b) {
    int result = a.getNamespace().compareTo(b.getNamespace());
    return result != 0 ? result : a.getPath().compareTo(b.getPath());
  }
}
