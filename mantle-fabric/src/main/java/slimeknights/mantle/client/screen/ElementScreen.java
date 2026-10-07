package slimeknights.mantle.client.screen;

import lombok.AllArgsConstructor;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

/**
 * Represents a GUI element INSIDE the graphics file.
 * The coordinates all refer to the coordinates inside the graphics!
 */
@AllArgsConstructor
public class ElementScreen implements slimeknights.mantle.client.screen.element.ScreenElement {
  // TODO: can this be final?
  public ResourceLocation texture;
  public final int x;
  public final int y;
  public final int w;
  public final int h;

  public final int texW;
  public final int texH;

  @Override
  public int width() {
    return w;
  }

  @Override
  public int height() {
    return h;
  }

  @Override
  public void drawInternal(GuiGraphics graphics, int xPos, int yPos, int blitOffset, int uOffset, int vOffset, int width, int height) {
    graphics.blit(this.texture, xPos, yPos, blitOffset, this.x + uOffset, this.y + vOffset, width, height, this.texW, this.texH);
  }

  /** Creates a new element from this texture with the X, Y, width, and height */
  public ElementScreen move(int x, int y, int width, int height) {
    return new ElementScreen(this.texture, x, y, width, height, this.texW, this.texH);
  }

  /** Creates a new element by offsetting this element by the given amount */
  public ElementScreen shift(int xd, int yd) {
    return move(x + xd, y + yd, this.w, this.h);
  }

  /**
   * Draws the element at the given x/y coordinates
   *
   * @param xPos X-Coordinate on the screen
   * @param yPos Y-Coordinate on the screen
   */
  @Override
  public void draw(GuiGraphics graphics, int xPos, int yPos, int blitOffset) {
    graphics.blit(this.texture, xPos, yPos, blitOffset, this.x, this.y, this.w, this.h, this.texW, this.texH);
  }

  /**
   * Draws the element at the given x/y coordinates
   *
   * @param xPos X-Coordinate on the screen
   * @param yPos Y-Coordinate on the screen
   */
  @Override
  public void draw(GuiGraphics graphics, int xPos, int yPos) {
    this.draw(graphics, xPos, yPos, 0);
  }
}
