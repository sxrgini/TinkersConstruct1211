package slimeknights.mantle.platform.menu;

import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;

import java.util.OptionalInt;
import java.util.function.Consumer;

/** Helpers for menus with extra opening data, replacing NeoForge's {@code IMenuTypeExtension} and {@code openMenu(provider, buf)}. */
public final class MenuTypes {
  private MenuTypes() {}

  /** Creates a menu type whose factory receives extra data from the server */
  public static <T extends AbstractContainerMenu> MenuType<T> create(IContainerFactory<T> factory) {
    return new ExtendedScreenHandlerType<T,byte[]>((id, inv, data) -> {
      RegistryFriendlyByteBuf buf = new RegistryFriendlyByteBuf(Unpooled.wrappedBuffer(data), inv.player.registryAccess());
      return factory.create(id, inv, buf);
    }, ByteBufCodecs.BYTE_ARRAY.mapStream(buf -> buf));
  }

  /** Opens a menu on the server, with extra data written for the client factory */
  public static OptionalInt openMenu(ServerPlayer player, MenuProvider provider, Consumer<RegistryFriendlyByteBuf> extraData) {
    return player.openMenu(new ExtendedScreenHandlerFactory<byte[]>() {
      @Override
      public byte[] getScreenOpeningData(ServerPlayer opener) {
        RegistryFriendlyByteBuf buf = new RegistryFriendlyByteBuf(Unpooled.buffer(), opener.registryAccess());
        extraData.accept(buf);
        byte[] bytes = new byte[buf.readableBytes()];
        buf.readBytes(bytes);
        return bytes;
      }

      @Override
      public Component getDisplayName() {
        return provider.getDisplayName();
      }

      @Override
      public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return provider.createMenu(id, inventory, player);
      }
    });
  }
}
