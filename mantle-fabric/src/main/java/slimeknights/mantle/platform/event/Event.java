package slimeknights.mantle.platform.event;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Base class for Forge style events fired on the {@link EventBus}. */
public abstract class Event {
  /** Marks an event as cancelable */
  @Retention(RetentionPolicy.RUNTIME)
  @Target(ElementType.TYPE)
  public @interface Cancelable {}

  /** Marks an event as having a result */
  @Retention(RetentionPolicy.RUNTIME)
  @Target(ElementType.TYPE)
  public @interface HasResult {}

  /** Result of an event */
  public enum Result {
    DENY, DEFAULT, ALLOW
  }

  private boolean canceled = false;
  private Result result = Result.DEFAULT;

  public boolean isCancelable() {
    for (Class<?> clazz = getClass(); clazz != null; clazz = clazz.getSuperclass()) {
      if (clazz.isAnnotationPresent(Cancelable.class)) {
        return true;
      }
    }
    return false;
  }

  public boolean isCanceled() {
    return canceled;
  }

  public void setCanceled(boolean canceled) {
    if (!isCancelable()) {
      throw new UnsupportedOperationException("Attempted to cancel a non cancelable event: " + getClass().getName());
    }
    this.canceled = canceled;
  }

  public boolean hasResult() {
    for (Class<?> clazz = getClass(); clazz != null; clazz = clazz.getSuperclass()) {
      if (clazz.isAnnotationPresent(HasResult.class)) {
        return true;
      }
    }
    return false;
  }

  public Result getResult() {
    return result;
  }

  public void setResult(Result result) {
    this.result = result;
  }
}
