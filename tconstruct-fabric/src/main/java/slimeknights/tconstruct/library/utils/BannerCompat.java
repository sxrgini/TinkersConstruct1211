package slimeknights.tconstruct.library.utils;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BannerPattern;
import slimeknights.mantle.util.GlobalRegistries;

import javax.annotation.Nullable;

/** Helpers for banner patterns, which in 1.21 are data driven and identified by ID instead of a short hash. */
public final class BannerCompat {
  private BannerCompat() {}

  /** Looks up a pattern by its ID string, null if missing or invalid */
  @Nullable
  public static Holder<BannerPattern> byId(HolderLookup.Provider registries, String id) {
    ResourceLocation name = ResourceLocation.tryParse(id);
    if (name == null) {
      return null;
    }
    return registries.lookupOrThrow(Registries.BANNER_PATTERN).get(ResourceKey.create(Registries.BANNER_PATTERN, name)).orElse(null);
  }

  /** Looks up a pattern by its ID string using the global registries */
  @Nullable
  public static Holder<BannerPattern> byId(String id) {
    return byId(GlobalRegistries.get(), id);
  }

  /** Gets the ID string used to store the pattern in NBT */
  public static String idOf(Holder<BannerPattern> pattern) {
    return pattern.unwrapKey().map(key -> key.location().toString()).orElseGet(() -> pattern.value().assetId().toString());
  }
}
