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
  /** Gets the vanilla block colors */
  public net.minecraft.client.color.block.BlockColors getBlockColors() {
    return net.minecraft.client.Minecraft.getInstance().getBlockColors();
  }

  /** Gets the vanilla item colors */
  public net.minecraft.client.color.item.ItemColors getItemColors() {
    return net.minecraft.client.Minecraft.getInstance().itemColors;
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
