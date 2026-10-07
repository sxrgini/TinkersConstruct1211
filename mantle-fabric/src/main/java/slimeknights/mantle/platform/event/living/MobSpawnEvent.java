package slimeknights.mantle.platform.event.living;

import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.level.ServerLevelAccessor;
import org.jetbrains.annotations.Nullable;
import slimeknights.mantle.platform.event.Event;

/** Mob spawn events, fired from mixins in {@code Mob} */
public abstract class MobSpawnEvent extends Event {
  private final Mob mob;
  private final ServerLevelAccessor level;

  protected MobSpawnEvent(Mob mob, ServerLevelAccessor level) {
    this.mob = mob;
    this.level = level;
  }

  public Mob getEntity() {
    return mob;
  }

  public ServerLevelAccessor getLevel() {
    return level;
  }

  /** Fired when a mob finalizes its spawn, canceling stops the vanilla finalize logic */
  @Cancelable
  public static class FinalizeSpawn extends MobSpawnEvent {
    private final DifficultyInstance difficulty;
    private final MobSpawnType spawnType;
    @Nullable
    private SpawnGroupData spawnData;

    public FinalizeSpawn(Mob mob, ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType spawnType, @Nullable SpawnGroupData spawnData) {
      super(mob, level);
      this.difficulty = difficulty;
      this.spawnType = spawnType;
      this.spawnData = spawnData;
    }

    public DifficultyInstance getDifficulty() {
      return difficulty;
    }

    public MobSpawnType getSpawnType() {
      return spawnType;
    }

    @Nullable
    public SpawnGroupData getSpawnData() {
      return spawnData;
    }

    public void setSpawnData(@Nullable SpawnGroupData spawnData) {
      this.spawnData = spawnData;
    }
  }
}
