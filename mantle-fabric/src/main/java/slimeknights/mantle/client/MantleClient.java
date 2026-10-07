package slimeknights.mantle.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import slimeknights.mantle.network.MantleNetwork;
import slimeknights.mantle.platform.network.PacketDistributor;

/** Client entrypoint for Mantle */
public class MantleClient implements ClientModInitializer {
  @Override
  public void onInitializeClient() {
    PacketDistributor.setClientSender(ClientPlayNetworking::send);
    MantleNetwork.registerClientHandlers();
    ClientEvents.init();
    slimeknights.mantle.platform.event.FabricClientEventBridge.init();
  }
}
