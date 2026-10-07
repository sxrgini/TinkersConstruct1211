package slimeknights.mantle.platform.client;

import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/** Registers vanilla reload listeners with Fabric, replacing NeoForge's {@code RegisterClientReloadListenersEvent}. */
public final class ClientReloadListeners {
  private ClientReloadListeners() {}

  /** Registers a client resource reload listener with the given ID */
  public static void register(ResourceLocation id, PreparableReloadListener listener) {
    if (listener instanceof IdentifiableResourceReloadListener) {
      ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener((IdentifiableResourceReloadListener) listener);
      return;
    }
    ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(new IdentifiableResourceReloadListener() {
      @Override
      public ResourceLocation getFabricId() {
        return id;
      }

      @Override
      public CompletableFuture<Void> reload(PreparationBarrier barrier, ResourceManager manager, ProfilerFiller prepareProfiler, ProfilerFiller applyProfiler, Executor prepareExecutor, Executor applyExecutor) {
        return listener.reload(barrier, manager, prepareProfiler, applyProfiler, prepareExecutor, applyExecutor);
      }
    });
  }
}
