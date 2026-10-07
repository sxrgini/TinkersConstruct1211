package slimeknights.mantle.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import slimeknights.mantle.platform.event.Event.Result;
import slimeknights.mantle.platform.event.EventBus;
import slimeknights.mantle.platform.event.living.LivingEntityUseItemEvent;
import slimeknights.mantle.platform.event.living.MobEffectEvent;

/** Fires mob effect and item use events on the Mantle bus */
@Mixin(LivingEntity.class)
public abstract class LivingEntityEffectEventsMixin {
  @WrapMethod(method = "canBeAffected")
  private boolean mantle$effectApplicable(MobEffectInstance effect, Operation<Boolean> original) {
    boolean result = original.call(effect);
    EventBus bus = EventBus.BUS;
    if (bus.hasListeners(MobEffectEvent.Applicable.class)) {
      MobEffectEvent.Applicable event = new MobEffectEvent.Applicable((LivingEntity) (Object) this, effect);
      bus.post(event);
      if (event.getResult() == Result.DENY) {
        return false;
      } else if (event.getResult() == Result.ALLOW) {
        return true;
      }
    }
    return result;
  }

  @WrapMethod(method = "addEffect(Lnet/minecraft/world/effect/MobEffectInstance;Lnet/minecraft/world/entity/Entity;)Z")
  private boolean mantle$effectAdded(MobEffectInstance effect, @Nullable Entity source, Operation<Boolean> original) {
    LivingEntity self = (LivingEntity) (Object) this;
    MobEffectInstance old = self.getEffect(effect.getEffect());
    boolean result = original.call(effect, source);
    EventBus bus = EventBus.BUS;
    if (result && bus.hasListeners(MobEffectEvent.Added.class)) {
      bus.post(new MobEffectEvent.Added(self, old, effect, source));
    }
    return result;
  }

  @WrapOperation(method = "completeUsingItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;finishUsingItem(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;)Lnet/minecraft/world/item/ItemStack;"))
  private ItemStack mantle$finishUsingItem(ItemStack stack, Level level, LivingEntity entity, Operation<ItemStack> original) {
    ItemStack copy = stack.copy();
    ItemStack result = original.call(stack, level, entity);
    EventBus bus = EventBus.BUS;
    if (bus.hasListeners(LivingEntityUseItemEvent.Finish.class)) {
      LivingEntityUseItemEvent.Finish event = new LivingEntityUseItemEvent.Finish(entity, copy, 0, result);
      bus.post(event);
      return event.getResultStack();
    }
    return result;
  }
}
