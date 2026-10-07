package slimeknights.mantle.platform.event.player;

import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.Nullable;
import slimeknights.mantle.platform.event.Event;
import slimeknights.mantle.platform.event.EventBus;

import java.util.List;

/** Fired when an item tooltip is built, bridged to Fabric's tooltip callback */
public class ItemTooltipEvent extends Event {
  private final ItemStack stack;
  private final Item.TooltipContext context;
  private final TooltipFlag flags;
  private final List<Component> tooltip;
  @Nullable
  private final Player player;

  public ItemTooltipEvent(ItemStack stack, Item.TooltipContext context, TooltipFlag flags, List<Component> tooltip, @Nullable Player player) {
    this.stack = stack;
    this.context = context;
    this.flags = flags;
    this.tooltip = tooltip;
    this.player = player;
  }

  public ItemStack getItemStack() { return stack; }
  public Item.TooltipContext getContext() { return context; }
  public TooltipFlag getFlags() { return flags; }
  public List<Component> getToolTip() { return tooltip; }
  @Nullable
  public Player getEntity() { return player; }

  /** Registers the bridge from Fabric's callback, call from the client initializer */
  public static void init() {
    ItemTooltipCallback.EVENT.register((stack, context, flag, lines) -> {
      if (EventBus.BUS.hasListeners(ItemTooltipEvent.class)) {
        EventBus.BUS.post(new ItemTooltipEvent(stack, context, flag, lines, net.minecraft.client.Minecraft.getInstance().player));
      }
    });
  }
}
