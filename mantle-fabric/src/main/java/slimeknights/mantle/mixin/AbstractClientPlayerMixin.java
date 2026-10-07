package slimeknights.mantle.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.client.player.AbstractClientPlayer;
import org.spongepowered.asm.mixin.Mixin;
import slimeknights.mantle.platform.event.EventBus;
import slimeknights.mantle.platform.event.client.ComputeFovModifierEvent;

/** Fires {@link ComputeFovModifierEvent} */
@Mixin(AbstractClientPlayer.class)
public class AbstractClientPlayerMixin {
  @WrapMethod(method = "getFieldOfViewModifier")
  private float mantle$fovModifier(Operation<Float> original) {
    float value = original.call();
    EventBus bus = EventBus.BUS;
    if (bus.hasListeners(ComputeFovModifierEvent.class)) {
      ComputeFovModifierEvent event = new ComputeFovModifierEvent((AbstractClientPlayer) (Object) this, value);
      bus.post(event);
      return event.getNewFovModifier();
    }
    return value;
  }
}
