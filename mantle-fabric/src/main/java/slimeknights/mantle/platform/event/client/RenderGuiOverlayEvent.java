package slimeknights.mantle.platform.event.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import slimeknights.mantle.platform.event.Event;

/** Fired around the rendering of vanilla gui overlays, only the hotbar is bridged currently */
public abstract class RenderGuiOverlayEvent extends Event {
  /** Vanilla overlay IDs */
  public enum VanillaGuiOverlay {
    HOTBAR("hotbar");

    private final ResourceLocation id;

    VanillaGuiOverlay(String name) {
      this.id = ResourceLocation.withDefaultNamespace(name);
    }

    public ResourceLocation type() {
      return id;
    }
  }

  private final GuiGraphics graphics;
  private final float partialTick;
  private final ResourceLocation overlay;

  protected RenderGuiOverlayEvent(GuiGraphics graphics, float partialTick, ResourceLocation overlay) {
    this.graphics = graphics;
    this.partialTick = partialTick;
    this.overlay = overlay;
  }

  public GuiGraphics getGuiGraphics() { return graphics; }
  public float getPartialTick() { return partialTick; }
  public ResourceLocation getOverlay() { return overlay; }

  /** Fired after an overlay renders */
  public static class Post extends RenderGuiOverlayEvent {
    public Post(GuiGraphics graphics, float partialTick, ResourceLocation overlay) {
      super(graphics, partialTick, overlay);
    }
  }
}
