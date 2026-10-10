package slimeknights.mantle.platform.event.client;

import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.minecraft.client.color.block.BlockColor;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import slimeknights.mantle.platform.event.Event;
import slimeknights.mantle.platform.event.lifecycle.IModBusEvent;

/** Registers block and item colors, backed by Fabric's color provider registry */
public abstract class RegisterColorHandlersEvent extends Event implements IModBusEvent {
  /** Block colors forwarding to Fabric, as the vanilla instance does not exist yet when mods initialize */
  private static final net.minecraft.client.color.block.BlockColors BLOCK_COLORS = new net.minecraft.client.color.block.BlockColors() {
    @Override
    public void register(BlockColor color, net.minecraft.world.level.block.Block... blocks) {
      ColorProviderRegistry.BLOCK.register(color, blocks);
    }
  };
  /** Item colors forwarding to Fabric, as the vanilla instance does not exist yet when mods initialize */
  private static final net.minecraft.client.color.item.ItemColors ITEM_COLORS = new net.minecraft.client.color.item.ItemColors() {
    @Override
    public void register(ItemColor color, ItemLike... items) {
      ColorProviderRegistry.ITEM.register(color, items);
    }
  };

  /** Gets a block colors instance that registers handlers, lookups are not supported */
  public net.minecraft.client.color.block.BlockColors getBlockColors() {
    return BLOCK_COLORS;
  }

  /** Gets an item colors instance that registers handlers, lookups are not supported */
  public net.minecraft.client.color.item.ItemColors getItemColors() {
    return ITEM_COLORS;
  }

  /** Registers block colors */
  public static class Block extends RegisterColorHandlersEvent {
    public void register(BlockColor color, net.minecraft.world.level.block.Block... blocks) {
      ColorProviderRegistry.BLOCK.register(color, blocks);
    }
  }

  /** Registers item colors */
  public static class Item extends RegisterColorHandlersEvent {
    public void register(ItemColor color, ItemLike... items) {
      ColorProviderRegistry.ITEM.register(color, items);
    }
  }
}
