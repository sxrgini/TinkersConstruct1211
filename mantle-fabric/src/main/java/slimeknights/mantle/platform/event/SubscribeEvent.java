package slimeknights.mantle.platform.event;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Marks a method as an event listener when its class or instance is registered on the {@link EventBus} */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface SubscribeEvent {
  EventPriority priority() default EventPriority.NORMAL;

  boolean receiveCanceled() default false;
}
