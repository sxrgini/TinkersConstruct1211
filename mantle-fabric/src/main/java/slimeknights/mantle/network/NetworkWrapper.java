package slimeknights.mantle.network;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;
import slimeknights.mantle.network.packet.IThreadsafePacket;
import slimeknights.mantle.platform.network.NetworkDirection;
import slimeknights.mantle.platform.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * Registration helper for Forge style packets, using the encode/decode constructor pattern, on top of Fabric payloads.
 * Packet ids are derived from the class name, so one wrapper can register any number of packets for its namespace.
 */
public class NetworkWrapper {
  private static final Map<Class<?>,Type<?>> TYPES = new ConcurrentHashMap<>();
  /** Registrations of client bound packets, applied from the client initializer */
  private static final List<Runnable> CLIENT_RECEIVERS = new ArrayList<>();

  private final String namespace;

  public NetworkWrapper(ResourceLocation channelName) {
    this.namespace = channelName.getNamespace();
  }

  /** Version is unused, Fabric negotiates payload types by id */
  public NetworkWrapper(ResourceLocation channelName, String version) {
    this(channelName);
  }

  /** Gets the payload type for the given packet class, looking through superclasses for enum constant bodies */
  public static Type<? extends CustomPacketPayload> typeOf(Class<?> clazz) {
    for (Class<?> check = clazz; check != null; check = check.getSuperclass()) {
      Type<?> type = TYPES.get(check);
      if (type != null) {
        return type;
      }
    }
    throw new IllegalStateException("Packet " + clazz.getName() + " has not been registered");
  }

  /** Registers a packet with a buffer constructor */
  @SuppressWarnings("unchecked")
  public <T extends IThreadsafePacket> void registerPacket(Class<T> clazz, Function<FriendlyByteBuf,T> decoder, @Nullable NetworkDirection direction) {
    String path = clazz.getSimpleName().replaceAll("([a-z0-9])([A-Z])", "$1_$2").toLowerCase(Locale.ROOT);
    Type<T> type = new Type<>(ResourceLocation.fromNamespaceAndPath(namespace, path));
    TYPES.put(clazz, type);
    StreamCodec<RegistryFriendlyByteBuf,T> codec = StreamCodec.of((buf, packet) -> packet.encode(buf), decoder::apply);
    if (direction == NetworkDirection.PLAY_TO_SERVER) {
      PayloadTypeRegistry.playC2S().register(type, codec);
      ServerPlayNetworking.registerGlobalReceiver(type, (packet, context) -> packet.handle(context::player));
    } else {
      PayloadTypeRegistry.playS2C().register(type, codec);
      synchronized (CLIENT_RECEIVERS) {
        CLIENT_RECEIVERS.add(() -> registerClient(type));
      }
    }
  }

  @Environment(EnvType.CLIENT)
  private static <T extends IThreadsafePacket> void registerClient(Type<T> type) {
    ClientPlayNetworking.registerGlobalReceiver(type, (packet, context) -> packet.handle(context::player));
  }

  /** Registers all client bound handlers, called from the client initializer */
  @Environment(EnvType.CLIENT)
  public static void registerClientReceivers() {
    synchronized (CLIENT_RECEIVERS) {
      CLIENT_RECEIVERS.forEach(Runnable::run);
    }
  }

  private static CustomPacketPayload payload(Object msg) {
    return (CustomPacketPayload) msg;
  }

  /** Sends a packet to the server */
  public void sendToServer(Object msg) {
    PacketDistributor.sendToServer(payload(msg));
  }

  /** Sends a vanilla packet to the given player */
  public void sendVanillaPacket(Packet<?> packet, Entity player) {
    if (player instanceof ServerPlayer serverPlayer) {
      serverPlayer.connection.send(packet);
    }
  }

  /** Sends a packet to the given player, does nothing if they are not a server player */
  public void sendTo(Object msg, Player player) {
    if (player instanceof ServerPlayer serverPlayer) {
      sendTo(msg, serverPlayer);
    }
  }

  /** Sends a packet to the given player */
  public void sendTo(Object msg, ServerPlayer player) {
    PacketDistributor.sendToPlayer(player, payload(msg));
  }

  /** Sends a packet to all players tracking the given position */
  public void sendToClientsAround(Object msg, ServerLevel level, BlockPos position) {
    for (ServerPlayer player : PlayerLookup.tracking(level, position)) {
      sendTo(msg, player);
    }
  }

  /** Sends a packet to all players tracking the entity, and the entity itself */
  public void sendToTrackingAndSelf(Object msg, Entity entity) {
    PacketDistributor.sendToPlayersTrackingEntityAndSelf(entity, payload(msg));
  }

  /** Sends a packet to all players tracking the entity */
  public void sendToTracking(Object msg, Entity entity) {
    PacketDistributor.sendToPlayersTrackingEntity(entity, payload(msg));
  }
}
