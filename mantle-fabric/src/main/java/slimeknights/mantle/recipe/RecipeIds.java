package slimeknights.mantle.recipe;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;

/**
 * Tracks the ID of loaded recipes, as recipes no longer know their ID in 1.21.
 * Lookups are by identity, so only recipe instances from the recipe manager have an ID.
 */
public final class RecipeIds {
  private static final Map<Recipe<?>,ResourceLocation> IDS = Collections.synchronizedMap(new IdentityHashMap<>());

  private RecipeIds() {}

  /** Hooks server reloads, called from mod init */
  public static void init() {
    ServerLifecycleEvents.SERVER_STARTED.register(server -> update(server.getRecipeManager()));
    ServerLifecycleEvents.END_DATA_PACK_RELOAD.register((server, resources, success) -> update(server.getRecipeManager()));
  }

  /** Refreshes the IDs from the given manager. Called both server side after reloads and client side after recipes sync. */
  public static void update(RecipeManager manager) {
    synchronized (IDS) {
      IDS.clear();
      for (RecipeHolder<?> holder : manager.getRecipes()) {
        IDS.put(holder.value(), holder.id());
      }
    }
  }

  /** Gets the ID of a loaded recipe, or null if the recipe is not from a recipe manager */
  @Nullable
  public static ResourceLocation get(Recipe<?> recipe) {
    return IDS.get(recipe);
  }
}
