package slimeknights.mantle.util;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.ItemStack;
import slimeknights.mantle.client.SafeClientAccess;

import javax.annotation.Nullable;

/**
 * Access to the current registry access for places where code lacks a level, such as legacy NBT serialization of stacks.
 * Prefer passing a provider from a level or packet when available.
 */
public final class GlobalRegistries {
  @Nullable
  private static volatile MinecraftServer server;

  private GlobalRegistries() {}

  /** Hooks server lifecycle, called from mod init */
  public static void init() {
    ServerLifecycleEvents.SERVER_STARTING.register(s -> server = s);
    ServerLifecycleEvents.SERVER_STOPPED.register(s -> server = null);
  }

  /** Gets the best available registry access */
  public static HolderLookup.Provider get() {
    MinecraftServer current = server;
    if (current != null) {
      return current.registryAccess();
    }
    RegistryAccess client = SafeClientAccess.getRegistryAccess();
    if (client != null) {
      return client;
    }
    return RegistryAccess.EMPTY;
  }

  /** Saves a stack to a tag using the global registries */
  public static Tag saveStack(ItemStack stack) {
    return stack.isEmpty() ? new CompoundTag() : stack.save(get());
  }

  /** Parses a stack from a tag using the global registries */
  public static ItemStack parseStack(CompoundTag tag) {
    return tag.isEmpty() ? ItemStack.EMPTY : ItemStack.parse(get(), tag).orElse(ItemStack.EMPTY);
  }
}
