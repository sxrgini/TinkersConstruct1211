package slimeknights.mantle.platform.client;

import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

/** Sprite pixel access, replacing NeoForge's {@code TextureAtlasSprite#getPixelRGBA} */
public final class SpriteHelper {
  private SpriteHelper() {}

  /** Gets the ABGR pixel color at the given position in the given animation frame */
  public static int getPixelRGBA(TextureAtlasSprite sprite, int frame, int x, int y) {
    SpriteContents contents = sprite.contents();
    int frameX = 0;
    int frameY = 0;
    if (contents.animatedTexture != null) {
      frameX = contents.animatedTexture.getFrameX(frame);
      frameY = contents.animatedTexture.getFrameY(frame);
    }
    return contents.originalImage.getPixelRGBA(frameX * contents.width() + x, frameY * contents.height() + y);
  }
}
