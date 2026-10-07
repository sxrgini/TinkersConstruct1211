package slimeknights.mantle.platform.client.model;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;

/** Vertex consumer that collects four vertices into a {@link BakedQuad}, replacing NeoForge's class of the same name. */
public class QuadBakingVertexConsumer implements VertexConsumer {
  private final int[] data = new int[IQuadTransformer.STRIDE * 4];
  private int vertex = -1;
  private TextureAtlasSprite sprite;
  private Direction direction = Direction.DOWN;
  private int tintIndex = -1;
  private boolean shade = true;

  public void setSprite(TextureAtlasSprite sprite) {
    this.sprite = sprite;
  }

  public void setDirection(Direction direction) {
    this.direction = direction;
  }

  public void setTintIndex(int tintIndex) {
    this.tintIndex = tintIndex;
  }

  public void setShade(boolean shade) {
    this.shade = shade;
  }

  /** Ambient occlusion is not stored in vanilla quads, kept for API compatibility */
  public void setHasAmbientOcclusion(boolean ambientOcclusion) {}

  /** Builds the quad from the four vertices added and resets for the next quad */
  public BakedQuad bakeQuad() {
    BakedQuad quad = new BakedQuad(data.clone(), tintIndex, direction, sprite, shade);
    vertex = -1;
    return quad;
  }

  private int base() {
    return vertex * IQuadTransformer.STRIDE;
  }

  @Override
  public VertexConsumer addVertex(float x, float y, float z) {
    vertex++;
    if (vertex > 3) {
      throw new IllegalStateException("Too many vertices for a quad");
    }
    int base = base();
    data[base] = Float.floatToRawIntBits(x);
    data[base + 1] = Float.floatToRawIntBits(y);
    data[base + 2] = Float.floatToRawIntBits(z);
    return this;
  }

  @Override
  public VertexConsumer setColor(int red, int green, int blue, int alpha) {
    // packed ABGR
    data[base() + IQuadTransformer.COLOR] = ((alpha & 0xFF) << 24) | ((blue & 0xFF) << 16) | ((green & 0xFF) << 8) | (red & 0xFF);
    return this;
  }

  @Override
  public VertexConsumer setUv(float u, float v) {
    data[base() + IQuadTransformer.UV0] = Float.floatToRawIntBits(u);
    data[base() + IQuadTransformer.UV0 + 1] = Float.floatToRawIntBits(v);
    return this;
  }

  @Override
  public VertexConsumer setUv1(int u, int v) {
    return this;
  }

  @Override
  public VertexConsumer setUv2(int u, int v) {
    data[base() + IQuadTransformer.UV2] = (u & 0xFFFF) | ((v & 0xFFFF) << 16);
    return this;
  }

  @Override
  public VertexConsumer setNormal(float x, float y, float z) {
    data[base() + IQuadTransformer.NORMAL] = ClientHooks.packNormal(x, y, z);
    return this;
  }
}
