package slimeknights.mantle.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

/** Block entity with additional utilities to make NBT syncing easier. */
public class MantleBlockEntity extends BlockEntity implements slimeknights.mantle.platform.capability.ICapabilityProvider {
  /** Capabilities provided by this block entity, empty by default. Subclasses override and chain to super. */
  @Override
  public <T> slimeknights.mantle.platform.capability.LazyOptional<T> getCapability(slimeknights.mantle.platform.capability.Capability<T> cap, @javax.annotation.Nullable net.minecraft.core.Direction side) {
    return slimeknights.mantle.platform.capability.LazyOptional.empty();
  }

  /** Called when the block entity is removed so capability optionals can be invalidated, replaces Forge's {@code invalidateCaps} */
  public void invalidateCaps() {}

  /** Called when the block entity is revived, replaces Forge's {@code reviveCaps} */
  public void reviveCaps() {}


  /** Extra data stored on the block entity, replacing NeoForge's persistent data */
  @Nullable
  private CompoundTag persistentData;

  /** Gets the persistent data, creating it if missing */
  public CompoundTag getPersistentData() {
    if (persistentData == null) {
      persistentData = new CompoundTag();
    }
    return persistentData;
  }

  /** Called when the block entity is loaded into a level, replaces Forge's {@code onLoad} */
  public void onLoad() {}

  @Override
  public void setLevel(net.minecraft.world.level.Level level) {
    super.setLevel(level);
    onLoad();
  }

  public MantleBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
    super(type, pos, state);
  }

  /** Checks if the level is not null and its serverside side */
  public boolean isServerSide() {
    return this.level != null && !level.isClientSide;
  }

  /** Checks if the level is not null and its client side */
  public boolean isClient() {
    return this.level != null && level.isClientSide;
  }

  /**
   * Marks the chunk dirty without performing comparator updates (twice!!) or block state checks
   * Used since most of our markDirty calls only adjust TE data
   * @see #setChanged()
   */
  public void setChangedFast() {
    if (level != null) {
      level.blockEntityChanged(worldPosition);
    }
  }
  
  
  /* Syncing */

  /**
   * If true, this TE syncs when {@link net.minecraft.world.level.Level#blockUpdated(BlockPos, Block) is called
   * Syncs data from {@link #saveSynced(CompoundTag, Provider) }
   */
  protected boolean shouldSyncOnUpdate() {
    return false;
  }

  @Override
  @Nullable
  public ClientboundBlockEntityDataPacket getUpdatePacket() {
    return shouldSyncOnUpdate() ? ClientboundBlockEntityDataPacket.create(this) : null;
  }

  /**
   * Write to NBT that is synced to the client in {@link #getUpdateTag(Provider)} and in {@link #saveAdditional(CompoundTag, Provider)}
   *
   * @param nbt         NBT
   * @param registries  Registry access for saving
   */
  protected void saveSynced(CompoundTag nbt, Provider registries) {}

  @Override
  public CompoundTag getUpdateTag(Provider registries) {
    CompoundTag nbt = new CompoundTag();
    saveSynced(nbt, registries);
    return nbt;
  }

  @Override
  public void saveAdditional(CompoundTag nbt, Provider registries) {
    super.saveAdditional(nbt, registries);
    saveSynced(nbt, registries);
    if (persistentData != null && !persistentData.isEmpty()) {
      nbt.put("PersistentData", persistentData.copy());
    }
  }

  @Override
  protected void loadAdditional(CompoundTag nbt, Provider registries) {
    super.loadAdditional(nbt, registries);
    if (nbt.contains("PersistentData", net.minecraft.nbt.Tag.TAG_COMPOUND)) {
      persistentData = nbt.getCompound("PersistentData").copy();
    }
  }
}
