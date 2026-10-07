package slimeknights.mantle.platform.client;

import com.mojang.blaze3d.shaders.FogShape;
import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandler;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.FogRenderer.FogMode;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import org.joml.Vector3f;
import slimeknights.mantle.platform.fluid.FluidStack;
import slimeknights.mantle.platform.fluid.FluidType;
import slimeknights.mantle.platform.fluid.FluidTypes;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

/**
 * Client rendering info for a fluid type, replacing NeoForge's {@code IClientFluidTypeExtensions}. Register with {@link #register(FluidType, IClientFluidTypeExtensions)}.
 * Textures and tint are hooked into Fabric's fluid renderer; fog and camera overlay methods are only used by Mantle's own renderers for now.
 */
public interface IClientFluidTypeExtensions {
  Map<FluidType,IClientFluidTypeExtensions> REGISTRY = new HashMap<>();
  IClientFluidTypeExtensions DEFAULT = new IClientFluidTypeExtensions() {};

  /** Registers extensions for a fluid type. Fabric render handlers are created once the game has started and all fluids are registered. */
  static void register(FluidType type, IClientFluidTypeExtensions extensions) {
    if (REGISTRY.isEmpty()) {
      ClientLifecycleEvents.CLIENT_STARTED.register(client -> registerRenderHandlers());
    }
    REGISTRY.put(type, extensions);
  }

  /** Gets the extensions for the given fluid */
  static IClientFluidTypeExtensions of(Fluid fluid) {
    FluidType type = FluidTypes.of(fluid);
    IClientFluidTypeExtensions extensions = REGISTRY.get(type);
    if (extensions != null) {
      return extensions;
    }
    if (type == FluidTypes.LAVA) {
      return LAVA;
    }
    return type == FluidTypes.WATER ? WATER : DEFAULT;
  }

  IClientFluidTypeExtensions WATER = new IClientFluidTypeExtensions() {
    @Override
    public ResourceLocation getStillTexture() {
      return ResourceLocation.withDefaultNamespace("block/water_still");
    }

    @Override
    public ResourceLocation getFlowingTexture() {
      return ResourceLocation.withDefaultNamespace("block/water_flow");
    }

    @Override
    public int getTintColor() {
      return 0xFF3F76E4;
    }
  };
  IClientFluidTypeExtensions LAVA = new IClientFluidTypeExtensions() {
    @Override
    public ResourceLocation getStillTexture() {
      return ResourceLocation.withDefaultNamespace("block/lava_still");
    }

    @Override
    public ResourceLocation getFlowingTexture() {
      return ResourceLocation.withDefaultNamespace("block/lava_flow");
    }
  };

  /** Tint color in ARGB */
  default int getTintColor() {
    return 0xFFFFFFFF;
  }

  default int getTintColor(FluidStack stack) {
    return getTintColor();
  }

  default int getTintColor(FluidState state, BlockAndTintGetter getter, BlockPos pos) {
    return getTintColor();
  }

  default ResourceLocation getStillTexture() {
    return ResourceLocation.withDefaultNamespace("block/water_still");
  }

  default ResourceLocation getStillTexture(FluidStack stack) {
    return getStillTexture();
  }

  default ResourceLocation getStillTexture(FluidState state, BlockAndTintGetter getter, BlockPos pos) {
    return getStillTexture();
  }

  default ResourceLocation getFlowingTexture() {
    return ResourceLocation.withDefaultNamespace("block/water_flow");
  }

  default ResourceLocation getFlowingTexture(FluidStack stack) {
    return getFlowingTexture();
  }

  default ResourceLocation getFlowingTexture(FluidState state, BlockAndTintGetter getter, BlockPos pos) {
    return getFlowingTexture();
  }

  @Nullable
  default ResourceLocation getOverlayTexture() {
    return null;
  }

  /** Texture rendered over the whole screen when the camera is inside the fluid */
  @Nullable
  default ResourceLocation getRenderOverlayTexture(Minecraft mc) {
    return null;
  }

  /** Renders a custom camera overlay */
  default void renderOverlay(Minecraft mc, PoseStack poseStack) {}

  default Vector3f modifyFogColor(Camera camera, float partialTick, ClientLevel level, int renderDistance, float darkenWorldAmount, Vector3f fluidFogColor) {
    return fluidFogColor;
  }

  default void modifyFogRender(Camera camera, FogMode mode, float renderDistance, float partialTick, float nearDistance, float farDistance, FogShape shape) {}

  /** Creates Fabric fluid render handlers for every fluid that has registered extensions */
  private static void registerRenderHandlers() {
    for (Fluid fluid : BuiltInRegistries.FLUID) {
      IClientFluidTypeExtensions extensions = REGISTRY.get(FluidTypes.of(fluid));
      if (extensions == null || fluid == Fluids.EMPTY) {
        continue;
      }
      FluidRenderHandlerRegistry.INSTANCE.register(fluid, new FluidRenderHandler() {
        @Override
        public TextureAtlasSprite[] getFluidSprites(@Nullable BlockAndTintGetter view, @Nullable BlockPos pos, FluidState state) {
          var atlas = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS);
          ResourceLocation still = view == null ? extensions.getStillTexture() : extensions.getStillTexture(state, view, pos);
          ResourceLocation flowing = view == null ? extensions.getFlowingTexture() : extensions.getFlowingTexture(state, view, pos);
          ResourceLocation overlay = extensions.getOverlayTexture();
          return new TextureAtlasSprite[] {atlas.apply(still), atlas.apply(flowing), overlay == null ? null : atlas.apply(overlay)};
        }

        @Override
        public int getFluidColor(@Nullable BlockAndTintGetter view, @Nullable BlockPos pos, FluidState state) {
          return view == null ? extensions.getTintColor() : extensions.getTintColor(state, view, pos);
        }
      });
    }
  }
}
