package slimeknights.mantle.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import slimeknights.mantle.platform.event.EventBus;
import slimeknights.mantle.platform.event.player.PlayerEvent;

/** Fires the break speed event with the position being broken, which {@code Player#getDestroySpeed} does not know about */
@Mixin(BlockBehaviour.class)
public abstract class BlockBehaviourEventsMixin {
  @WrapOperation(method = "getDestroyProgress", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;getDestroySpeed(Lnet/minecraft/world/level/block/state/BlockState;)F"))
  private float mantle$breakSpeed(Player player, BlockState state, Operation<Float> original, BlockState stateArg, Player playerArg, BlockGetter level, BlockPos pos) {
    float speed = original.call(player, state);
    EventBus bus = EventBus.BUS;
    if (bus.hasListeners(PlayerEvent.BreakSpeed.class)) {
      PlayerEvent.BreakSpeed event = new PlayerEvent.BreakSpeed(player, state, speed, pos);
      if (bus.post(event)) {
        return -1;
      }
      return event.getNewSpeed();
    }
    return speed;
  }
}
