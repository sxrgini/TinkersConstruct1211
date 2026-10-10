package slimeknights.mantle.recipe;

import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.crafting.RecipeHolder;
import slimeknights.mantle.Mantle;

import java.util.ArrayList;
import java.util.List;

/**
 * Removes recipes that cannot be written to the network, such as recipes whose output is a tag that is empty with the current mods.
 * Forge tolerated these, but on 1.21 a single bad recipe kicks every player on join as the whole recipe list is sent in one packet.
 */
public final class RecipeSyncSanitizer {
  private RecipeSyncSanitizer() {}

  /** Registers the listeners */
  public static void init() {
    ServerLifecycleEvents.SERVER_STARTED.register(RecipeSyncSanitizer::sanitize);
    ServerLifecycleEvents.END_DATA_PACK_RELOAD.register((server, manager, success) -> sanitize(server));
  }

  private static void sanitize(MinecraftServer server) {
    List<RecipeHolder<?>> valid = new ArrayList<>();
    int removed = 0;
    for (RecipeHolder<?> holder : server.getRecipeManager().getRecipes()) {
      try {
        RecipeHolder.STREAM_CODEC.encode(new RegistryFriendlyByteBuf(Unpooled.buffer(), server.registryAccess()), holder);
        valid.add(holder);
      } catch (RuntimeException e) {
        removed++;
        Mantle.logger.warn("Removed recipe {} as it cannot be synced to clients: {}", holder.id(), e.getMessage());
      }
    }
    if (removed > 0) {
      server.getRecipeManager().replaceRecipes(valid);
    }
  }
}
