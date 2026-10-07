package slimeknights.mantle.platform.event.player;

import slimeknights.mantle.platform.event.Event.Result;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import slimeknights.mantle.platform.event.Event.Cancelable;

import javax.annotation.Nullable;

/** Base for player interaction events */
@Cancelable
public class PlayerInteractEvent extends PlayerEvent {
  private final InteractionHand hand;
  private final BlockPos pos;
  @Nullable
  private final Direction face;
  private InteractionResult cancellationResult = InteractionResult.PASS;

  protected PlayerInteractEvent(Player player, InteractionHand hand, BlockPos pos, @Nullable Direction face) {
    super(player);
    this.hand = hand;
    this.pos = pos;
    this.face = face;
  }

  public InteractionHand getHand() {
    return hand;
  }

  public ItemStack getItemStack() {
    return getEntity().getItemInHand(hand);
  }

  public BlockPos getPos() {
    return pos;
  }

  @Nullable
  public Direction getFace() {
    return face;
  }

  public Level getLevel() {
    return getEntity().level();
  }

  public InteractionResult getCancellationResult() {
    return cancellationResult;
  }

  public void setCancellationResult(InteractionResult result) {
    this.cancellationResult = result;
  }

  /** Right click on a block */
  public static class RightClickBlock extends PlayerInteractEvent {
    private final BlockHitResult hitVec;

    public RightClickBlock(Player player, InteractionHand hand, BlockPos pos, BlockHitResult hitVec) {
      super(player, hand, pos, hitVec.getDirection());
      this.hitVec = hitVec;
    }

    public BlockHitResult getHitVec() {
      return hitVec;
    }

    private Result useItem = Result.DEFAULT;
    private Result useBlock = Result.DEFAULT;

    public Result getUseItem() {
      return useItem;
    }

    public void setUseItem(Result useItem) {
      this.useItem = useItem;
    }

    public Result getUseBlock() {
      return useBlock;
    }

    public void setUseBlock(Result useBlock) {
      this.useBlock = useBlock;
    }
  }

  /** Left click on a block */
  public static class LeftClickBlock extends PlayerInteractEvent {
    /** Stage of the left click */
    public enum Action {
      START, STOP, ABORT, CLIENT_HOLD
    }

    private final Action action;

    public LeftClickBlock(Player player, BlockPos pos, Direction face, Action action) {
      super(player, InteractionHand.MAIN_HAND, pos, face);
      this.action = action;
    }

    public Action getAction() {
      return action;
    }
  }

  /** Right click while not targeting anything, client only */
  public static class RightClickEmpty extends PlayerInteractEvent {
    public RightClickEmpty(Player player, InteractionHand hand) {
      super(player, hand, player.blockPosition(), null);
    }
  }

  /** Left click while not targeting anything, client only */
  public static class LeftClickEmpty extends PlayerInteractEvent {
    public LeftClickEmpty(Player player) {
      super(player, InteractionHand.MAIN_HAND, player.blockPosition(), null);
    }
  }

  /** Right click on an entity */
  public static class EntityInteract extends PlayerInteractEvent {
    private final Entity target;

    public EntityInteract(Player player, InteractionHand hand, Entity target) {
      super(player, hand, target.blockPosition(), null);
      this.target = target;
    }

    public Entity getTarget() {
      return target;
    }
  }
}
