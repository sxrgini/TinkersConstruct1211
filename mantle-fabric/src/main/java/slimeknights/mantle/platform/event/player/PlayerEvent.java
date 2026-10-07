package slimeknights.mantle.platform.event.player;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import slimeknights.mantle.platform.event.Event.Cancelable;
import slimeknights.mantle.platform.event.living.LivingEvent;

import javax.annotation.Nullable;
import java.util.Optional;

/** Base for events involving a player */
public class PlayerEvent extends LivingEvent {
  public PlayerEvent(Player player) {
    super(player);
  }

  @Override
  public Player getEntity() {
    return (Player) super.getEntity();
  }

  /** Fired when computing how fast a player breaks a block */
  @Cancelable
  public static class BreakSpeed extends PlayerEvent {
    private final BlockState state;
    private final float originalSpeed;
    private float newSpeed;
    @Nullable
    private final BlockPos pos;

    public BreakSpeed(Player player, BlockState state, float original, @Nullable BlockPos pos) {
      super(player);
      this.state = state;
      this.originalSpeed = original;
      this.newSpeed = original;
      this.pos = pos;
    }

    public BlockState getState() {
      return state;
    }

    public float getOriginalSpeed() {
      return originalSpeed;
    }

    public float getNewSpeed() {
      return newSpeed;
    }

    public void setNewSpeed(float newSpeed) {
      this.newSpeed = newSpeed;
    }

    public Optional<BlockPos> getPosition() {
      return Optional.ofNullable(pos);
    }
  }

  /** Fired when a player entity is copied on death or return from the end, see Fabric's COPY_FROM */
  public static class Clone extends PlayerEvent {
    private final Player original;
    private final boolean wasDeath;

    public Clone(Player player, Player original, boolean wasDeath) {
      super(player);
      this.original = original;
      this.wasDeath = wasDeath;
    }

    public Player getOriginal() {
      return original;
    }

    public boolean isWasDeath() {
      return wasDeath;
    }
  }

  /** Fired after a player respawns */
  public static class PlayerRespawnEvent extends PlayerEvent {
    private final boolean endConquered;

    public PlayerRespawnEvent(Player player, boolean endConquered) {
      super(player);
      this.endConquered = endConquered;
    }

    public boolean isEndConquered() {
      return endConquered;
    }
  }

  /** Fired after a player changes dimension */
  public static class PlayerChangedDimensionEvent extends PlayerEvent {
    private final net.minecraft.resources.ResourceKey<net.minecraft.world.level.Level> from;
    private final net.minecraft.resources.ResourceKey<net.minecraft.world.level.Level> to;

    public PlayerChangedDimensionEvent(Player player, net.minecraft.resources.ResourceKey<net.minecraft.world.level.Level> from, net.minecraft.resources.ResourceKey<net.minecraft.world.level.Level> to) {
      super(player);
      this.from = from;
      this.to = to;
    }

    public net.minecraft.resources.ResourceKey<net.minecraft.world.level.Level> getFrom() {
      return from;
    }

    public net.minecraft.resources.ResourceKey<net.minecraft.world.level.Level> getTo() {
      return to;
    }
  }

  /** Fired when a player joins the server */
  public static class PlayerLoggedInEvent extends PlayerEvent {
    public PlayerLoggedInEvent(Player player) {
      super(player);
    }
  }

  /** Fired when a player leaves the server */
  public static class PlayerLoggedOutEvent extends PlayerEvent {
    public PlayerLoggedOutEvent(Player player) {
      super(player);
    }
  }

  /** Fired when a player starts tracking another entity */
  public static class StartTracking extends PlayerEvent {
    private final net.minecraft.world.entity.Entity target;

    public StartTracking(Player player, net.minecraft.world.entity.Entity target) {
      super(player);
      this.target = target;
    }

    public net.minecraft.world.entity.Entity getTarget() {
      return target;
    }
  }
}
