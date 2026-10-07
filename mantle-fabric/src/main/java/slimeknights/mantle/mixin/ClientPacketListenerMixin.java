package slimeknights.mantle.mixin;

import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundUpdateRecipesPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import slimeknights.mantle.platform.event.EventBus;
import slimeknights.mantle.platform.event.client.RecipesUpdatedEvent;

/** Fires {@link RecipesUpdatedEvent} after the recipe list is synced */
@Mixin(ClientPacketListener.class)
public abstract class ClientPacketListenerMixin {
  @Shadow
  public abstract net.minecraft.world.item.crafting.RecipeManager getRecipeManager();

  @Inject(method = "handleUpdateRecipes", at = @At("TAIL"))
  private void mantle$recipesUpdated(ClientboundUpdateRecipesPacket packet, CallbackInfo ci) {
    EventBus bus = EventBus.BUS;
    if (bus.hasListeners(RecipesUpdatedEvent.class)) {
      bus.post(new RecipesUpdatedEvent(getRecipeManager()));
    }
  }
}
