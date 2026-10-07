package slimeknights.mantle.platform.event.server;

import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import slimeknights.mantle.Mantle;
import slimeknights.mantle.platform.condition.ICondition.IContext;
import slimeknights.mantle.platform.event.Event;
import slimeknights.mantle.platform.event.EventBus;
import slimeknights.mantle.util.DataLoadedConditionContext;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * Collects server data reload listeners. Fabric has no per reload event, so this is fired once with {@link #fire()} after all mods have registered
 * their listeners on the bus, and the collected listeners are registered with Fabric in order.
 */
public class AddReloadListenerEvent extends Event {
  private final java.util.ArrayList<PreparableReloadListener> listeners = new java.util.ArrayList<>();
  private static boolean fired = false;

  /** Adds a listener to the data pack reload */
  public void addListener(PreparableReloadListener listener) {
    listeners.add(listener);
  }

  /** Gets the context used to test conditions in loaded data */
  public IContext getConditionContext() {
    return DataLoadedConditionContext.INSTANCE;
  }

  /** Gets the listeners added to the event */
  public List<PreparableReloadListener> getListeners() {
    return listeners;
  }

  /** Fires the event and registers all listeners with Fabric. Safe to call multiple times, only the first call does anything. */
  public static void fire() {
    if (fired) {
      return;
    }
    fired = true;
    AddReloadListenerEvent event = new AddReloadListenerEvent();
    EventBus.BUS.post(event);
    ResourceLocation previous = null;
    int index = 0;
    for (PreparableReloadListener listener : event.listeners) {
      ResourceLocation id = listener instanceof IdentifiableResourceReloadListener identifiable
        ? identifiable.getFabricId()
        : ResourceLocation.fromNamespaceAndPath(Mantle.modId, "wrapped/" + index + "_" + listener.getClass().getSimpleName().toLowerCase(java.util.Locale.ROOT));
      index++;
      ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(new Wrapper(id, previous, listener));
      previous = id;
    }
  }

  private record Wrapper(ResourceLocation id, ResourceLocation dependency, PreparableReloadListener listener) implements IdentifiableResourceReloadListener {
    @Override
    public ResourceLocation getFabricId() {
      return id;
    }

    @Override
    public java.util.Collection<ResourceLocation> getFabricDependencies() {
      return dependency == null ? List.of() : List.of(dependency);
    }

    @Override
    public CompletableFuture<Void> reload(PreparationBarrier barrier, ResourceManager manager, ProfilerFiller prepareProfiler, ProfilerFiller applyProfiler, Executor prepareExecutor, Executor applyExecutor) {
      return listener.reload(barrier, manager, prepareProfiler, applyProfiler, prepareExecutor, applyExecutor);
    }

    @Override
    public String getName() {
      return listener.getName();
    }
  }
}
