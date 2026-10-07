package slimeknights.mantle.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import slimeknights.mantle.platform.event.EventBus;
import slimeknights.mantle.platform.event.living.LivingAttackEvent;
import slimeknights.mantle.platform.event.living.LivingDamageEvent;
import slimeknights.mantle.platform.event.living.LivingEvent;
import slimeknights.mantle.platform.event.living.LivingExperienceDropEvent;
import slimeknights.mantle.platform.event.living.LivingFallEvent;
import slimeknights.mantle.platform.event.living.LivingGetProjectileEvent;
import slimeknights.mantle.platform.event.living.LivingHurtEvent;
import slimeknights.mantle.platform.event.living.LivingKnockBackEvent;

/**
 * Fires Forge style living entity events at the same points Forge's patches do.
 * {@code Player} overrides {@code actuallyHurt} with its own copy of the damage logic, so the hurt and damage events hook the shared helper methods instead.
 */
@Mixin(LivingEntity.class)
public abstract class LivingEntityEventsMixin {
  @Inject(method = "hurt", at = @At("HEAD"), cancellable = true)
  private void mantle$livingAttack(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
    EventBus bus = EventBus.BUS;
    if (bus.hasListeners(LivingAttackEvent.class) && bus.post(new LivingAttackEvent((LivingEntity) (Object) this, source, amount))) {
      cir.setReturnValue(false);
    }
  }

  /** Hurt event, fired before armor reduction */
  @WrapMethod(method = "getDamageAfterArmorAbsorb")
  private float mantle$livingHurt(DamageSource source, float amount, Operation<Float> original) {
    EventBus bus = EventBus.BUS;
    if (bus.hasListeners(LivingHurtEvent.class)) {
      LivingHurtEvent event = new LivingHurtEvent((LivingEntity) (Object) this, source, amount);
      if (bus.post(event)) {
        return 0;
      }
      amount = event.getAmount();
    }
    return original.call(source, amount);
  }

  /** Damage event, fired after armor and magic reduction but before absorption */
  @WrapMethod(method = "getDamageAfterMagicAbsorb")
  private float mantle$livingDamage(DamageSource source, float amount, Operation<Float> original) {
    float result = original.call(source, amount);
    EventBus bus = EventBus.BUS;
    if (bus.hasListeners(LivingDamageEvent.class)) {
      LivingDamageEvent event = new LivingDamageEvent((LivingEntity) (Object) this, source, result);
      if (bus.post(event)) {
        return 0;
      }
      return event.getAmount();
    }
    return result;
  }

  @WrapMethod(method = "knockback")
  private void mantle$livingKnockback(double strength, double x, double z, Operation<Void> original) {
    EventBus bus = EventBus.BUS;
    if (bus.hasListeners(LivingKnockBackEvent.class)) {
      LivingKnockBackEvent event = new LivingKnockBackEvent((LivingEntity) (Object) this, (float) strength, x, z);
      if (bus.post(event)) {
        return;
      }
      original.call((double) event.getStrength(), event.getRatioX(), event.getRatioZ());
    } else {
      original.call(strength, x, z);
    }
  }

  @WrapMethod(method = "causeFallDamage")
  private boolean mantle$livingFall(float distance, float multiplier, DamageSource source, Operation<Boolean> original) {
    EventBus bus = EventBus.BUS;
    if (bus.hasListeners(LivingFallEvent.class)) {
      LivingFallEvent event = new LivingFallEvent((LivingEntity) (Object) this, distance, multiplier);
      if (bus.post(event)) {
        return false;
      }
      return original.call(event.getDistance(), event.getDamageMultiplier(), source);
    }
    return original.call(distance, multiplier, source);
  }

  @WrapMethod(method = "getVisibilityPercent")
  private double mantle$livingVisibility(Entity looking, Operation<Double> original) {
    double visibility = original.call(looking);
    EventBus bus = EventBus.BUS;
    if (bus.hasListeners(LivingEvent.LivingVisibilityEvent.class)) {
      LivingEvent.LivingVisibilityEvent event = new LivingEvent.LivingVisibilityEvent((LivingEntity) (Object) this, looking, visibility);
      bus.post(event);
      return Math.max(0, event.getVisibilityModifier());
    }
    return visibility;
  }

  @WrapMethod(method = "getProjectile")
  private ItemStack mantle$livingProjectile(ItemStack weapon, Operation<ItemStack> original) {
    ItemStack projectile = original.call(weapon);
    EventBus bus = EventBus.BUS;
    if (bus.hasListeners(LivingGetProjectileEvent.class)) {
      LivingGetProjectileEvent event = new LivingGetProjectileEvent((LivingEntity) (Object) this, weapon, projectile);
      bus.post(event);
      return event.getProjectileItemStack();
    }
    return projectile;
  }

  @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
  private void mantle$livingTick(CallbackInfo ci) {
    EventBus bus = EventBus.BUS;
    if (bus.hasListeners(LivingEvent.LivingTickEvent.class) && bus.post(new LivingEvent.LivingTickEvent((LivingEntity) (Object) this))) {
      ci.cancel();
    }
  }

  @Inject(method = "jumpFromGround", at = @At("RETURN"))
  private void mantle$livingJump(CallbackInfo ci) {
    EventBus bus = EventBus.BUS;
    if (bus.hasListeners(LivingEvent.LivingJumpEvent.class)) {
      bus.post(new LivingEvent.LivingJumpEvent((LivingEntity) (Object) this));
    }
  }

  @WrapOperation(method = "dropExperience", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;getExperienceReward(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/Entity;)I"))
  private int mantle$livingExperience(LivingEntity self, ServerLevel level, Entity killer, Operation<Integer> original) {
    int reward = original.call(self, level, killer);
    EventBus bus = EventBus.BUS;
    if (bus.hasListeners(LivingExperienceDropEvent.class)) {
      LivingExperienceDropEvent event = new LivingExperienceDropEvent(self, killer instanceof net.minecraft.world.entity.player.Player player ? player : null, reward, reward);
      if (bus.post(event)) {
        return 0;
      }
      return event.getDroppedExperience();
    }
    return reward;
  }
}
