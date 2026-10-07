package slimeknights.mantle.platform.capability;

import com.mojang.serialization.Codec;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import slimeknights.mantle.Mantle;
import slimeknights.mantle.platform.event.AttachCapabilitiesEvent;
import slimeknights.mantle.platform.event.EventBus;

import javax.annotation.Nullable;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Capability lookup for entities, item stacks and block entities, replacing Forge's {@code ICapabilityProvider} methods on those classes.
 * <ul>
 *   <li>Entities: providers added through {@link AttachCapabilitiesEvent}, stored in a persistent Fabric attachment so serializable providers keep their data.</li>
 *   <li>Item stacks: the item's {@code initCapabilities(ItemStack, CompoundTag)} method (found by reflection) plus event providers, cached per stack instance.</li>
 *   <li>Block entities: the block entity's own {@code getCapability(Capability, Direction)} method (found by reflection) or {@link ICapabilityProvider} implementation.</li>
 * </ul>
 */
public final class Caps {
  private Caps() {}

  /* Entities */

  /** Providers attached to an entity, plus data saved for providers that are not built yet */
  private static final class Store {
    @Nullable
    private Map<ResourceLocation,ICapabilityProvider> providers;
    private CompoundTag saved;

    private Store(CompoundTag saved) {
      this.saved = saved;
    }

    private CompoundTag toTag() {
      if (providers == null) {
        return saved;
      }
      CompoundTag tag = new CompoundTag();
      providers.forEach((id, provider) -> {
        if (provider instanceof ICapabilitySerializable<?> serializable) {
          Tag data = serializable.serializeNBT();
          if (data != null) {
            tag.put(id.toString(), data);
          }
        }
      });
      return tag;
    }
  }

  private static final Codec<Store> STORE_CODEC = CompoundTag.CODEC.xmap(Store::new, Store::toTag);
  private static final AttachmentType<Store> ENTITY_STORE = AttachmentRegistry.<Store>builder()
    .persistent(STORE_CODEC)
    .buildAndRegister(Mantle.getResource("capabilities"));

  @SuppressWarnings({"unchecked", "rawtypes"})
  private static Map<ResourceLocation,ICapabilityProvider> entityProviders(Entity entity) {
    Store store = entity.getAttachedOrCreate(ENTITY_STORE, () -> new Store(new CompoundTag()));
    if (store.providers == null) {
      EventBus bus = EventBus.BUS;
      Map<ResourceLocation,ICapabilityProvider> providers = Collections.emptyMap();
      if (bus.hasListeners(AttachCapabilitiesEvent.class)) {
        AttachCapabilitiesEvent<Entity> event = new AttachCapabilitiesEvent<>(entity);
        bus.post(event);
        providers = new LinkedHashMap<>(event.getCapabilities());
      }
      // mark as built before deserializing so providers may query capabilities
      store.providers = providers;
      CompoundTag saved = store.saved;
      for (Map.Entry<ResourceLocation,ICapabilityProvider> entry : providers.entrySet()) {
        if (entry.getValue() instanceof ICapabilitySerializable serializable) {
          String key = entry.getKey().toString();
          if (saved.contains(key)) {
            serializable.deserializeNBT(saved.get(key));
          }
        }
      }
      store.saved = new CompoundTag();
    }
    return store.providers;
  }

  /** Gets a capability from an entity */
  public static <T> LazyOptional<T> get(Entity entity, Capability<T> cap) {
    return get(entity, cap, null);
  }

  /** Gets a capability from an entity */
  public static <T> LazyOptional<T> get(Entity entity, Capability<T> cap, @Nullable Direction side) {
    for (ICapabilityProvider provider : entityProviders(entity).values()) {
      LazyOptional<T> result = provider.getCapability(cap, side);
      if (result.isPresent()) {
        return result;
      }
    }
    return LazyOptional.empty();
  }


  /* Item stacks */

  private static final Map<ItemStack,Map<ResourceLocation,ICapabilityProvider>> STACKS = new WeakHashMap<>();
  private static final Map<Class<?>,Object> ITEM_METHODS = new ConcurrentHashMap<>();
  private static final Object NO_METHOD = new Object();

  @Nullable
  private static Method findMethod(Map<Class<?>,Object> cache, Class<?> clazz, String name, Class<?>... params) {
    Object cached = cache.computeIfAbsent(clazz, c -> {
      for (Class<?> current = c; current != null && current != Object.class; current = current.getSuperclass()) {
        try {
          Method method = current.getDeclaredMethod(name, params);
          method.setAccessible(true);
          return method;
        } catch (NoSuchMethodException ignored) {}
      }
      return NO_METHOD;
    });
    return cached == NO_METHOD ? null : (Method) cached;
  }

  private static Map<ResourceLocation,ICapabilityProvider> stackProviders(ItemStack stack) {
    synchronized (STACKS) {
      Map<ResourceLocation,ICapabilityProvider> existing = STACKS.get(stack);
      if (existing != null) {
        return existing;
      }
    }
    Map<ResourceLocation,ICapabilityProvider> providers = new LinkedHashMap<>();
    Method init = findMethod(ITEM_METHODS, stack.getItem().getClass(), "initCapabilities", ItemStack.class, CompoundTag.class);
    if (init != null) {
      try {
        Object provider = init.invoke(stack.getItem(), stack, null);
        if (provider instanceof ICapabilityProvider capabilityProvider) {
          providers.put(Mantle.getResource("item"), capabilityProvider);
        }
      } catch (ReflectiveOperationException e) {
        throw new IllegalStateException("Failed to create capabilities for " + stack.getItem(), e);
      }
    }
    EventBus bus = EventBus.BUS;
    if (bus.hasListeners(AttachCapabilitiesEvent.class)) {
      AttachCapabilitiesEvent<ItemStack> event = new AttachCapabilitiesEvent<>(stack);
      bus.post(event);
      providers.putAll(event.getCapabilities());
    }
    synchronized (STACKS) {
      STACKS.put(stack, providers);
    }
    return providers;
  }

  /** Gets a capability from an item stack */
  public static <T> LazyOptional<T> get(ItemStack stack, Capability<T> cap) {
    if (stack.isEmpty()) {
      return LazyOptional.empty();
    }
    for (ICapabilityProvider provider : stackProviders(stack).values()) {
      LazyOptional<T> result = provider.getCapability(cap, null);
      if (result.isPresent()) {
        return result;
      }
    }
    return LazyOptional.empty();
  }


  /* Block entities */

  private static final Map<Class<?>,Object> BLOCK_ENTITY_METHODS = new ConcurrentHashMap<>();

  /** Gets a capability from a block entity */
  @SuppressWarnings("unchecked")
  public static <T> LazyOptional<T> get(BlockEntity be, Capability<T> cap, @Nullable Direction side) {
    if (be instanceof ICapabilityProvider provider) {
      return provider.getCapability(cap, side);
    }
    Method method = findMethod(BLOCK_ENTITY_METHODS, be.getClass(), "getCapability", Capability.class, Direction.class);
    if (method != null) {
      try {
        return (LazyOptional<T>) method.invoke(be, cap, side);
      } catch (ReflectiveOperationException e) {
        throw new IllegalStateException("Failed to query capabilities of " + be, e);
      }
    }
    return LazyOptional.empty();
  }

  /** Gets a capability from a block entity */
  public static <T> LazyOptional<T> get(BlockEntity be, Capability<T> cap) {
    return get(be, cap, null);
  }
}
