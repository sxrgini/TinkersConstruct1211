package slimeknights.mantle.mixin;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.crafting.RecipeManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import slimeknights.mantle.platform.condition.ICondition;
import slimeknights.mantle.util.DataLoadedConditionContext;
import slimeknights.mantle.util.JsonHelper;

import java.util.Map;

/** Evaluates Mantle load conditions on recipe JSON before the recipe manager parses it */
@Mixin(RecipeManager.class)
public class RecipeManagerMixin {
  @Inject(method = "apply(Ljava/util/Map;Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)V", at = @At("HEAD"))
  private void mantle$filterConditions(Map<ResourceLocation,JsonElement> map, ResourceManager manager, ProfilerFiller profiler, CallbackInfo ci) {
    ICondition.IContext context = DataLoadedConditionContext.INSTANCE;
    map.entrySet().removeIf(entry -> {
      if (entry.getValue() instanceof JsonObject json) {
        return !JsonHelper.processConditions(json, "neoforge:conditions", context) || !JsonHelper.processConditions(json, "conditions", context);
      }
      return false;
    });
  }
}
