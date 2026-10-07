package slimeknights.mantle.client.render;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import lombok.Getter;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ShaderInstance;
import net.fabricmc.fabric.api.client.rendering.v1.CoreShaderRegistrationCallback;
import slimeknights.mantle.Mantle;
import slimeknights.mantle.config.Config;

import javax.annotation.Nullable;
import java.io.IOException;

/** Handles any custom shaders registered by Mantle. */
public class MantleShaders {
  /** Shader used for blocks in structures to force them fullbright. Based on ... */
  @Nullable
  @Getter
  private static ShaderInstance blockFullBrightShader;
  /** Shader used for fluids in block entity renderers. Based on {@link GameRenderer#positionColorTexLightmapShader} nad {@link GameRenderer#rendertypeEntityTranslucentCullShader} */
  @Nullable
  @Getter
  private static ShaderInstance fluidShader;

  /** Gets the shader to use for {@link MantleRenderTypes#FLUID_SHADER}, checking the config option to select which shader to use. */
  @Nullable
  public static ShaderInstance getConfiguredFluidShader() {
    if (Config.ENABLE_FLUID_FOG_FIX.get()) {
      return fluidShader;
    }
    return Config.FLUID_USE_TEXT_SHADER.get() ? GameRenderer.getRendertypeTextShader() : GameRenderer.getPositionColorTexLightmapShader();
  }

  /** Registers the shaders with Fabric, call from the client initializer */
  public static void init() {
    CoreShaderRegistrationCallback.EVENT.register(context -> {
      context.register(Mantle.getResource("block_fullbright"), DefaultVertexFormat.BLOCK, shader -> blockFullBrightShader = shader);
      context.register(Mantle.getResource("fluid"), DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP, shader -> fluidShader = shader);
    });
  }
}
