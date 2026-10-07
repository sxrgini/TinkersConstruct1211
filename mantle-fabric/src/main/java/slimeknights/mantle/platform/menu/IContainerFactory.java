package slimeknights.mantle.platform.menu;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;

/** Menu factory that receives extra data written by the server, mirroring NeoForge's {@code IContainerFactory}. */
@FunctionalInterface
public interface IContainerFactory<T extends AbstractContainerMenu> {
  T create(int windowId, Inventory inv, RegistryFriendlyByteBuf data);
}
