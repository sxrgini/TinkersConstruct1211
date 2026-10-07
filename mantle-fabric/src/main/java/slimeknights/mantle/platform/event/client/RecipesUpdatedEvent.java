package slimeknights.mantle.platform.event.client;

import net.minecraft.world.item.crafting.RecipeManager;
import slimeknights.mantle.platform.event.Event;

/** Fired on the client when the recipe list is synced from the server */
public class RecipesUpdatedEvent extends Event {
  private final RecipeManager manager;

  public RecipesUpdatedEvent(RecipeManager manager) {
    this.manager = manager;
  }

  public RecipeManager getRecipeManager() { return manager; }
}
