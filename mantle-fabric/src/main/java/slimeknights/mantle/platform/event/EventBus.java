package slimeknights.mantle.platform.event;

import slimeknights.mantle.Mantle;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * Small Forge style event bus. Fabric has no equivalent for most gameplay events, so Mantle fires these from mixins and mods listen here.
 * Listeners must be registered with an explicit event class, as lambda parameter types cannot be inferred without Forge's type resolver.
 */
public final class EventBus {
  /** The main gameplay bus, equivalent of {@code MinecraftForge.EVENT_BUS} */
  public static final EventBus BUS = new EventBus();

  private record Listener(EventPriority priority, boolean receiveCanceled, Consumer<Event> consumer) {}

  private final Map<Class<?>,List<Listener>> listeners = new ConcurrentHashMap<>();
  private final Map<Class<?>,List<Listener>> cache = new ConcurrentHashMap<>();

  /** Bus for mod lifecycle events (common setup, client setup, data gathering), equivalent of the Forge mod event bus */
  public static final EventBus MOD_BUS = new EventBus();

  private EventBus() {}

  /** Adds a listener for the given event class */
  @SuppressWarnings("unchecked")
  public <T extends Event> void addListener(EventPriority priority, boolean receiveCanceled, Class<T> type, Consumer<T> consumer) {
    listeners.computeIfAbsent(type, t -> new CopyOnWriteArrayList<>()).add(new Listener(priority, receiveCanceled, (Consumer<Event>) consumer));
    cache.clear();
  }

  /** Adds a listener for the given event class with normal priority */
  public <T extends Event> void addListener(Class<T> type, Consumer<T> consumer) {
    addListener(EventPriority.NORMAL, false, type, consumer);
  }

  /** Adds a listener for the given event class with the given priority */
  public <T extends Event> void addListener(EventPriority priority, Class<T> type, Consumer<T> consumer) {
    addListener(priority, false, type, consumer);
  }

  /** Adds a listener for a generic attach capabilities event, filtered to objects of the given type */
  public <T> void addGenericListener(Class<T> type, Consumer<AttachCapabilitiesEvent<T>> consumer) {
    addListener(EventPriority.NORMAL, false, AttachCapabilitiesEvent.class, (AttachCapabilitiesEvent event) -> {
      if (type.isInstance(event.getObject())) {
        consumer.accept((AttachCapabilitiesEvent<T>) event);
      }
    });
  }

  /** Registers the static {@link SubscribeEvent} methods of the class */
  public void register(Class<?> clazz) {
    registerMethods(clazz, null);
  }

  /** Registers the instance {@link SubscribeEvent} methods of the object, and its static methods */
  public void register(Object object) {
    registerMethods(object.getClass(), object);
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  private void registerMethods(Class<?> clazz, Object instance) {
    for (Method method : clazz.getDeclaredMethods()) {
      SubscribeEvent annotation = method.getAnnotation(SubscribeEvent.class);
      if (annotation == null) {
        continue;
      }
      boolean isStatic = Modifier.isStatic(method.getModifiers());
      if (instance == null && !isStatic) {
        continue;
      }
      Class<?>[] params = method.getParameterTypes();
      if (params.length != 1 || !Event.class.isAssignableFrom(params[0])) {
        throw new IllegalArgumentException("Method " + method + " has @SubscribeEvent but does not take exactly one event parameter");
      }
      method.setAccessible(true);
      Object target = isStatic ? null : instance;
      addListener(annotation.priority(), annotation.receiveCanceled(), (Class) params[0], (Consumer<Event>) event -> {
        try {
          method.invoke(target, event);
        } catch (ReflectiveOperationException e) {
          throw new IllegalStateException("Failed to invoke event listener " + method, e.getCause() == null ? e : e.getCause());
        }
      });
    }
  }

  /** True if anything is listening for the given event class or a parent of it, allows skipping event creation in hot paths */
  public boolean hasListeners(Class<? extends Event> type) {
    return !getListeners(type).isEmpty();
  }

  private List<Listener> getListeners(Class<?> type) {
    return cache.computeIfAbsent(type, t -> {
      List<Listener> found = new ArrayList<>();
      for (Class<?> clazz = t; clazz != null && Event.class.isAssignableFrom(clazz); clazz = clazz.getSuperclass()) {
        List<Listener> list = listeners.get(clazz);
        if (list != null) {
          found.addAll(list);
        }
      }
      found.sort(Comparator.comparing(l -> l.priority));
      return found;
    });
  }

  /**
   * Posts an event to all listeners.
   * @return  True if the event was canceled
   */
  public boolean post(Event event) {
    for (Listener listener : getListeners(event.getClass())) {
      if (event.isCanceled() && !listener.receiveCanceled) {
        continue;
      }
      try {
        listener.consumer.accept(event);
      } catch (RuntimeException e) {
        Mantle.logger.error("Exception while handling event {}", event.getClass().getName(), e);
        throw e;
      }
    }
    return event.isCancelable() && event.isCanceled();
  }
}
