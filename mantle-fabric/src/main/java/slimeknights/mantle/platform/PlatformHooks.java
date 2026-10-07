package slimeknights.mantle.platform;

import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.registry.FuelRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.Nullable;
import slimeknights.mantle.platform.event.Event;
import slimeknights.mantle.platform.event.EventBus;
import slimeknights.mantle.platform.event.entity.ProjectileImpactEvent;
import slimeknights.mantle.platform.event.living.LivingFallEvent;
import slimeknights.mantle.platform.event.living.LivingGetProjectileEvent;
import slimeknights.mantle.platform.event.player.CriticalHitEvent;
import slimeknights.mantle.platform.event.player.PlayerInteractEvent;

import java.util.List;

/** Replacement for the Forge hook and event factory methods Tinkers calls, implemented on Fabric events or Mantle's event bus. */
public final class PlatformHooks {
  private PlatformHooks() {}

  private static final ThreadLocal<Player> CRAFTING_PLAYER = new ThreadLocal<>();

  /** Sets the player currently crafting, used by recipes that need to know who is crafting */
  public static void setCraftingPlayer(@Nullable Player player) {
    CRAFTING_PLAYER.set(player);
  }

  /** Gets the player currently crafting */
  @Nullable
  public static Player getCraftingPlayer() {
    return CRAFTING_PLAYER.get();
  }

  /** Fires the projectile selection event and returns the final ammo stack */
  public static ItemStack getProjectile(LivingEntity entity, ItemStack weapon, ItemStack ammo) {
    LivingGetProjectileEvent event = new LivingGetProjectileEvent(entity, weapon, ammo);
    EventBus.BUS.post(event);
    return event.getProjectileItemStack();
  }

  /** Gets the burn time of the stack in ticks, or 0 if not a fuel */
  public static int getBurnTime(ItemStack stack, @Nullable RecipeType<?> type) {
    Integer time = FuelRegistry.INSTANCE.get(stack.getItem());
    return time == null ? 0 : time;
  }

  /** Runs break callbacks before a block is broken by a player, returns -1 if canceled otherwise the experience to drop */
  public static int onBlockBreakEvent(Level level, GameType gameType, net.minecraft.server.level.ServerPlayer player, BlockPos pos) {
    if (gameType == GameType.SPECTATOR) {
      return -1;
    }
    if (!PlayerBlockBreakEvents.BEFORE.invoker().beforeBlockBreak(level, player, pos, level.getBlockState(pos), level.getBlockEntity(pos))) {
      PlayerBlockBreakEvents.CANCELED.invoker().onBlockBreakCanceled(level, player, pos, level.getBlockState(pos), level.getBlockEntity(pos));
      return -1;
    }
    // vanilla has no way to ask a block for its experience, so listeners start from zero and may only add to it
    slimeknights.mantle.platform.event.level.BlockEvent.BreakEvent event = new slimeknights.mantle.platform.event.level.BlockEvent.BreakEvent(level, pos, level.getBlockState(pos), player, 0);
    if (EventBus.BUS.post(event)) {
      return -1;
    }
    return event.getExpToDrop();
  }

  /** Fires the right click block event */
  public static PlayerInteractEvent.RightClickBlock onRightClickBlock(Player player, InteractionHand hand, BlockPos pos, BlockHitResult hit) {
    PlayerInteractEvent.RightClickBlock event = new PlayerInteractEvent.RightClickBlock(player, hand, pos, hit);
    EventBus.BUS.post(event);
    return event;
  }

  /** Fires the entity interact callbacks, returns the result of the interaction */
  public static InteractionResult onInteractEntityAt(Player player, Entity target, HitResult hit, InteractionHand hand) {
    EntityHitResult entityHit = hit instanceof EntityHitResult result ? result : null;
    return UseEntityCallback.EVENT.invoker().interact(player, player.level(), hand, target, entityHit);
  }

  /** Fires the fall event, returns the distance and multiplier or null if canceled */
  @Nullable
  public static float[] onLivingFall(LivingEntity entity, float distance, float multiplier) {
    LivingFallEvent event = new LivingFallEvent(entity, distance, multiplier);
    if (EventBus.BUS.post(event)) {
      return null;
    }
    return new float[] { event.getDistance(), event.getDamageMultiplier() };
  }

  /** Fires the critical hit event, returns null if the hit was denied */
  @Nullable
  public static CriticalHitEvent getCriticalHit(Player player, Entity target, boolean vanillaCritical, float damageModifier) {
    CriticalHitEvent event = new CriticalHitEvent(player, target, damageModifier, vanillaCritical);
    EventBus.BUS.post(event);
    if (event.getResult() == Event.Result.DENY || (!vanillaCritical && event.getResult() != Event.Result.ALLOW)) {
      return null;
    }
    return event;
  }

  /** Fires the projectile impact event, returns true if the impact should be skipped */
  public static boolean onProjectileImpact(Projectile projectile, HitResult hit) {
    ProjectileImpactEvent event = new ProjectileImpactEvent(projectile, hit);
    return EventBus.BUS.post(event) || event.getImpactResult() == ProjectileImpactEvent.ImpactResult.SKIP_ENTITY;
  }

  /** Fabric has no crafting event, kept as a hook for future bridging */
  public static void firePlayerCraftingEvent(Player player, ItemStack crafted, Object inventory) {}

  /** Fabric has no item destroy event, kept as a hook for future bridging */
  public static void onPlayerDestroyItem(Player player, ItemStack stack, @Nullable InteractionHand hand) {}

  /** Fabric has no explosion start event, returns false as the explosion is never canceled */
  public static boolean onExplosionStart(Level level, Explosion explosion) {
    return false;
  }

  /** Fabric has no explosion detonate event */
  public static void onExplosionDetonate(Level level, Explosion explosion, List<Entity> affected, double diameter) {}

  /** Fabric has no arrow nock event, returns null to continue vanilla behavior */
  @Nullable
  public static net.minecraft.world.InteractionResultHolder<ItemStack> onArrowNock(ItemStack bow, Level level, Player player, InteractionHand hand, boolean hasAmmo) {
    return null;
  }

  /** Fabric has no arrow loose event, returns the charge unchanged */
  public static int onArrowLoose(ItemStack bow, Level level, Player player, int charge, boolean hasAmmo) {
    return charge;
  }

  /** Opens a menu on the server with the position written for the client factory */
  public static void openScreen(net.minecraft.server.level.ServerPlayer player, net.minecraft.world.MenuProvider provider, BlockPos pos) {
    slimeknights.mantle.platform.menu.MenuTypes.openMenu(player, provider, buf -> buf.writeBlockPos(pos));
  }

  /** Opens a menu on the server with arbitrary extra data written for the client factory */
  public static void openScreen(net.minecraft.server.level.ServerPlayer player, net.minecraft.world.MenuProvider provider, java.util.function.Consumer<net.minecraft.network.RegistryFriendlyByteBuf> extraData) {
    slimeknights.mantle.platform.menu.MenuTypes.openMenu(player, provider, extraData);
  }

  /** Opens a menu on the server with no extra data */
  public static void openScreen(net.minecraft.server.level.ServerPlayer player, net.minecraft.world.MenuProvider provider) {
    slimeknights.mantle.platform.menu.MenuTypes.openMenu(player, provider, buf -> {});
  }

  /**
   * Uses a block as a player would, trying the held item first and then the empty hand interaction, replacing {@code BlockState#use}.
   * Unlike a real interaction this does not run item use.
   */
  public static net.minecraft.world.InteractionResult useBlock(net.minecraft.world.level.block.state.BlockState state, Level level, Player player, InteractionHand hand, net.minecraft.world.phys.BlockHitResult hit) {
    net.minecraft.world.ItemInteractionResult itemResult = state.useItemOn(player.getItemInHand(hand), level, player, hand, hit);
    if (itemResult.consumesAction()) {
      return itemResult.result();
    }
    if (itemResult == net.minecraft.world.ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION && hand == InteractionHand.MAIN_HAND) {
      return state.useWithoutItem(level, player, hit);
    }
    return itemResult.result();
  }
}
