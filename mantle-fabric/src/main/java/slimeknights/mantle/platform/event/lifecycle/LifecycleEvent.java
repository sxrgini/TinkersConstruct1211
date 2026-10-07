package slimeknights.mantle.platform.event.lifecycle;

import slimeknights.mantle.platform.event.Event;

/** Base for mod lifecycle events posted on {@link slimeknights.mantle.platform.event.EventBus#MOD_BUS} */
public abstract class LifecycleEvent extends Event {
  /** Runs the work immediately, Fabric has no parallel setup phase */
  public void enqueueWork(Runnable work) {
    work.run();
  }
}
