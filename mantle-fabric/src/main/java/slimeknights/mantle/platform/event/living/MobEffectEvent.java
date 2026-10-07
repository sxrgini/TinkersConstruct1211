package slimeknights.mantle.platform.event.living;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

/** Events for mob effects, fired from mixins in {@code LivingEntity} */
public class MobEffectEvent extends LivingEvent {
  @Nullable
  private final MobEffectInstance effect;

  public MobEffectEvent(LivingEntity living, @Nullable MobEffectInstance effect) {
    super(living);
    this.effect = effect;
  }

  @Nullable
  public MobEffectInstance getEffectInstance() {
    return effect;
  }

  /** Fired to check if an effect can be applied, set the result to deny to block it */
  @HasResult
  public static class Applicable extends MobEffectEvent {
    public Applicable(LivingEntity living, MobEffectInstance effect) {
      super(living, effect);
    }
  }

  /** Fired after an effect is added to an entity, the old instance is the one replaced if any */
  public static class Added extends MobEffectEvent {
    @Nullable
    private final MobEffectInstance oldEffect;
    @Nullable
    private final Entity source;

    public Added(LivingEntity living, @Nullable MobEffectInstance oldEffect, MobEffectInstance newEffect, @Nullable Entity source) {
      super(living, newEffect);
      this.oldEffect = oldEffect;
      this.source = source;
    }

    @Nullable
    public MobEffectInstance getOldEffectInstance() {
      return oldEffect;
    }

    @Nullable
    public Entity getEffectSource() {
      return source;
    }
  }

  /** Fired when an effect is removed before it expires */
  @Cancelable
  public static class Remove extends MobEffectEvent {
    public Remove(LivingEntity living, MobEffectInstance effect) {
      super(living, effect);
    }
  }

  /** Fired when an effect runs out */
  public static class Expired extends MobEffectEvent {
    public Expired(LivingEntity living, MobEffectInstance effect) {
      super(living, effect);
    }
  }
}
