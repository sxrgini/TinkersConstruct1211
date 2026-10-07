package slimeknights.mantle.platform.event.client;

import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import slimeknights.mantle.platform.event.Event;
import slimeknights.mantle.platform.event.lifecycle.IModBusEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

/** Entity renderer registration events, backed by Fabric's rendering registries */
public abstract class EntityRenderersEvent extends Event implements IModBusEvent {
  /** Registers entity and block entity renderers */
  public static class RegisterRenderers extends EntityRenderersEvent {
    public <T extends Entity> void registerEntityRenderer(EntityType<? extends T> type, EntityRendererProvider<T> provider) {
      EntityRendererRegistry.register(type, provider);
    }

    public <T extends BlockEntity> void registerBlockEntityRenderer(BlockEntityType<? extends T> type, BlockEntityRendererProvider<T> provider) {
      BlockEntityRenderers.register(type, provider);
    }
  }

  /** Registers model layer definitions */
  public static class RegisterLayerDefinitions extends EntityRenderersEvent {
    public void registerLayerDefinition(ModelLayerLocation layer, Supplier<LayerDefinition> supplier) {
      EntityModelLayerRegistry.registerModelLayer(layer, supplier::get);
    }
  }

  /** Allows registering skull models, the map is read by the skull renderer mixin */
  public static class CreateSkullModels extends EntityRenderersEvent {
    private static final Map<Object,Function<net.minecraft.client.model.geom.EntityModelSet,? extends Model>> MODELS = new HashMap<>();

    /** Registers a skull model factory for the skull type */
    public void registerSkullModel(Object skullType, ModelLayerLocation layer) {
      MODELS.put(skullType, set -> new net.minecraft.client.model.SkullModel(set.bakeLayer(layer)));
    }

    /** Gets the registered factories */
    public static Map<Object,Function<net.minecraft.client.model.geom.EntityModelSet,? extends Model>> getModels() {
      return MODELS;
    }
  }
}
