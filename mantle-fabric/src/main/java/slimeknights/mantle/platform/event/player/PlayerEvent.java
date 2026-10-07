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
}
