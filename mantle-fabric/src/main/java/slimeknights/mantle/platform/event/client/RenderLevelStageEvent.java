package slimeknights.mantle.platform.event.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.LevelRenderer;
import org.joml.Matrix4f;
import slimeknights.mantle.platform.event.Event;

/** Fired at stages of level rendering, bridged to Fabric's world render events */
public class RenderLevelStageEvent extends Event {
  /** Stages of level rendering that are bridged */
  public enum Stage {
    AFTER_TRIPWIRE_BLOCKS,
    AFTER_ENTITIES,
    AFTER_PARTICLES
  }

  private final Stage stage;
  private final LevelRenderer renderer;
  private final PoseStack poseStack;
  private final Matrix4f projection;
  private final float partialTick;
  private final Camera camera;

  public RenderLevelStageEvent(Stage stage, LevelRenderer renderer, PoseStack poseStack, Matrix4f projection, float partialTick, Camera camera) {
    this.stage = stage;
    this.renderer = renderer;
    this.poseStack = poseStack;
    this.projection = projection;
    this.partialTick = partialTick;
    this.camera = camera;
  }

  public Stage getStage() { return stage; }
  public LevelRenderer getLevelRenderer() { return renderer; }
  public PoseStack getPoseStack() { return poseStack; }
  public Matrix4f getProjectionMatrix() { return projection; }
  public float getPartialTick() { return partialTick; }
  public Camera getCamera() { return camera; }
}
