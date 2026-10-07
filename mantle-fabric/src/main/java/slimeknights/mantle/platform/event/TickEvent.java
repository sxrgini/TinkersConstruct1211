package slimeknights.mantle.platform.event;

import net.minecraft.world.entity.player.Player;

/** Tick events fired by the Fabric tick callbacks, equivalent of Forge's TickEvent */
public abstract class TickEvent extends Event {
  public enum Phase {
    START, END
  }

  public final Phase phase;
  public final LogicalSide side;

  protected TickEvent(Phase phase, LogicalSide side) {
    this.phase = phase;
    this.side = side;
  }

  /** Fired for each player at the start and end of the tick */
  public static class PlayerTickEvent extends TickEvent {
    public final Player player;

    public PlayerTickEvent(Phase phase, LogicalSide side, Player player) {
      super(phase, side);
      this.player = player;
    }
  }
}
