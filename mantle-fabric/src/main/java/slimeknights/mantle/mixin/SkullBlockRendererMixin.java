package slimeknights.mantle.mixin;

import net.minecraft.client.model.SkullModelBase;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.blockentity.SkullBlockRenderer;
import net.minecraft.world.level.block.SkullBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import slimeknights.mantle.platform.event.EventBus;
import slimeknights.mantle.platform.event.client.EntityRenderersEvent;

import java.util.HashMap;
import java.util.Map;

/** Fires {@link EntityRenderersEvent.CreateSkullModels} when vanilla creates its skull model map */
@Mixin(SkullBlockRenderer.class)
public class SkullBlockRendererMixin {
  @Inject(method = "createSkullRenderers", at = @At("RETURN"), cancellable = true)
  private static void mantle$addSkullModels(EntityModelSet modelSet, CallbackInfoReturnable<Map<SkullBlock.Type,SkullModelBase>> cir) {
    Map<SkullBlock.Type,SkullModelBase> models = new HashMap<>(cir.getReturnValue());
    EventBus.MOD_BUS.post(new EntityRenderersEvent.CreateSkullModels(modelSet, models));
    cir.setReturnValue(models);
  }
}
