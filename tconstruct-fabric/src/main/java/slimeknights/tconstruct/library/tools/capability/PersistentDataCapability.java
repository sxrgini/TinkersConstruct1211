package slimeknights.tconstruct.library.tools.capability;

import slimeknights.mantle.platform.capability.Caps;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import slimeknights.mantle.platform.event.EventBus;
import slimeknights.mantle.platform.capability.Capability;
import slimeknights.mantle.platform.capability.ICapabilitySerializable;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import slimeknights.mantle.platform.util.Lazy;
import slimeknights.mantle.platform.capability.LazyOptional;
import slimeknights.mantle.platform.event.AttachCapabilitiesEvent;
import slimeknights.mantle.platform.event.player.PlayerEvent;
import slimeknights.mantle.platform.event.EventPriority;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.common.network.SyncPersistentDataPacket;
import slimeknights.tconstruct.common.network.TinkerNetwork;
import slimeknights.tconstruct.library.tools.nbt.ModDataNBT;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;

/**
 * Capability to store persistent NBT data on an entity. For players, this is automatically synced to the client on load, but not during gameplay.
 * Persists after death, will reassess if we need some data to not persist death
 */
public class PersistentDataCapability {
  private PersistentDataCapability() {}

  /** Capability ID */
  private static final ResourceLocation ID = TConstruct.getResource("persistent_data");
  /** Capability type */
  public static final Capability<ModDataNBT> CAPABILITY = new Capability<>("capability");

  /** Gets the data or warns if its missing */
  public static ModDataNBT getOrWarn(Entity entity) {
    Optional<ModDataNBT> data = Caps.get(entity, CAPABILITY).resolve();
    if (data.isEmpty()) {
      TConstruct.LOG.warn("Missing Tinkers NBT on entity {}, this should not happen", entity.getType());
      return new ModDataNBT();
    }
    return data.get();
  }

  /** Registers this capability */
  public static void register() {
    EventBus.MOD_BUS.addListener(EventPriority.NORMAL, false, RegisterCapabilitiesEvent.class, PersistentDataCapability::register);
    EventBus.BUS.addGenericListener(Entity.class, PersistentDataCapability::attachCapability);
    EventBus.BUS.addListener(EventPriority.NORMAL, false, PlayerEvent.Clone.class, PersistentDataCapability::playerClone);
    EventBus.BUS.addListener(EventPriority.NORMAL, false, PlayerEvent.PlayerRespawnEvent.class, PersistentDataCapability::playerRespawn);
    EventBus.BUS.addListener(EventPriority.NORMAL, false, PlayerEvent.PlayerChangedDimensionEvent.class, PersistentDataCapability::playerChangeDimension);
    EventBus.BUS.addListener(EventPriority.NORMAL, false, PlayerEvent.PlayerLoggedInEvent.class, PersistentDataCapability::playerLoggedIn);
  }

  /** Registers the capability with the event bus */
  private static void register(RegisterCapabilitiesEvent event) {
    event.register(ModDataNBT.class);
  }

  /** Event listener to attach the capability */
  private static void attachCapability(AttachCapabilitiesEvent<Entity> event) {
    Entity entity = event.getObject();
    // must be on living entities as we use this for potions, but also support anything else with modifiers, this is their data
    if (entity instanceof LivingEntity || EntityModifierCapability.supportCapability(entity)) {
      Provider provider = new Provider();
      event.addCapability(ID, provider);
      event.addListener(provider);
    }
  }

  /** Syncs the data to the given player */
  private static void sync(Player player) {
    Caps.get(player, CAPABILITY).ifPresent(data -> TinkerNetwork.getInstance().sendTo(new SyncPersistentDataPacket(data.getCopy()), player));
  }

  /** copy caps when the player respawns/returns from the end */
  private static void playerClone(PlayerEvent.Clone event) {
    Player original = event.getOriginal();
    original.reviveCaps();
    Caps.get(original, CAPABILITY).ifPresent(oldData -> {
      CompoundTag nbt = oldData.getCopy();
      if (!nbt.isEmpty()) {
        Caps.get(event.getEntity(), CAPABILITY).ifPresent(newData -> newData.copyFrom(nbt));
      }
    });
    original.invalidateCaps();
  }

  /** sync caps when the player respawns/returns from the end */
  private static void playerRespawn(PlayerEvent.PlayerRespawnEvent event) {
    sync(event.getEntity());
  }

  /** sync caps when the player changes dimensions */
  private static void playerChangeDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
    sync(event.getEntity());
  }

  /** sync caps when the player logs in */
  private static void playerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
    sync(event.getEntity());
  }

  /** Capability provider instance */
  private static class Provider implements ICapabilitySerializable<CompoundTag>, Runnable {
    private Lazy<CompoundTag> nbt;
    private LazyOptional<ModDataNBT> capability;
    private Provider() {
      this.nbt = Lazy.of(CompoundTag::new);
      this.capability = LazyOptional.of(() -> ModDataNBT.readFromNBT(nbt.get()));
    }

    @Nonnull
    
    public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side) {
      return CAPABILITY.orEmpty(cap, capability);
    }

    @Override
    public void run() {
      // called when capabilities invalidate, create a new cap just in case they are revived later
      capability.invalidate();
      capability = LazyOptional.of(() -> ModDataNBT.readFromNBT(nbt.get()));
    }

    @Override
    public CompoundTag serializeNBT() {
      return nbt.get().copy();
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
      this.nbt = Lazy.of(() -> nbt);
      run();
    }
  }
}
