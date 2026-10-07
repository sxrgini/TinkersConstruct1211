package slimeknights.mantle.network.packet;

import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.networking.v1.EntityTrackingEvents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.Entity;
import slimeknights.mantle.Mantle;
import slimeknights.mantle.network.ISimplePacket;
import slimeknights.mantle.platform.network.IEntityAdditionalSpawnData;
import slimeknights.mantle.platform.network.IPayloadContext;
import slimeknights.mantle.platform.network.PacketDistributor;

/** Syncs the additional spawn data of an entity to a client that started tracking it */
public record AdditionalSpawnDataPacket(int entityId, byte[] data) implements ISimplePacket {
  public static final Type<AdditionalSpawnDataPacket> TYPE = new Type<>(Mantle.getResource("additional_spawn_data"));
  public static final StreamCodec<RegistryFriendlyByteBuf,AdditionalSpawnDataPacket> CODEC = StreamCodec.composite(
    ByteBufCodecs.VAR_INT, AdditionalSpawnDataPacket::entityId,
    ByteBufCodecs.BYTE_ARRAY, AdditionalSpawnDataPacket::data,
    AdditionalSpawnDataPacket::new);

  /** Registers the tracking callback to send the data, call from the common initializer */
  public static void init() {
    EntityTrackingEvents.START_TRACKING.register((entity, player) -> {
      if (entity instanceof IEntityAdditionalSpawnData spawnData) {
        RegistryFriendlyByteBuf buffer = new RegistryFriendlyByteBuf(Unpooled.buffer(), entity.registryAccess());
        spawnData.writeSpawnData(buffer);
        byte[] bytes = new byte[buffer.readableBytes()];
        buffer.readBytes(bytes);
        PacketDistributor.sendToPlayer(player, new AdditionalSpawnDataPacket(entity.getId(), bytes));
      }
    });
  }

  @Override
  public Type<AdditionalSpawnDataPacket> type() {
    return TYPE;
  }

  @Override
  public void handle(IPayloadContext context) {
    Entity entity = context.player().level().getEntity(entityId);
    if (entity instanceof IEntityAdditionalSpawnData spawnData) {
      spawnData.readSpawnData(new RegistryFriendlyByteBuf(Unpooled.wrappedBuffer(data), entity.registryAccess()));
    }
  }
}
