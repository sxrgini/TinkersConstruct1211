package slimeknights.mantle.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.level.ServerLevelAccessor;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import slimeknights.mantle.platform.event.EventBus;
import slimeknights.mantle.platform.event.living.MobSpawnEvent;

/** Fires the finalize spawn event */
@Mixin(Mob.class)
public abstract class MobSpawnEventsMixin {
  @Nullable
  @WrapMethod(method = "finalizeSpawn")
  private SpawnGroupData mantle$finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType type, @Nullable SpawnGroupData data, Operation<SpawnGroupData> original) {
    EventBus bus = EventBus.BUS;
    if (bus.hasListeners(MobSpawnEvent.FinalizeSpawn.class)) {
      MobSpawnEvent.FinalizeSpawn event = new MobSpawnEvent.FinalizeSpawn((Mob) (Object) this, level, difficulty, type, data);
      if (bus.post(event)) {
        return null;
      }
      data = event.getSpawnData();
    }
    return original.call(level, difficulty, type, data);
  }
}
