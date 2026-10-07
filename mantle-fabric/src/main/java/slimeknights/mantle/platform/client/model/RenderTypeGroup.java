package slimeknights.mantle.platform.client.model;

import net.minecraft.client.renderer.RenderType;

import javax.annotation.Nullable;

/** Hint for render types. Fabric picks block layers from the block registration, so this is only carried for API compatibility. */
public record RenderTypeGroup(@Nullable RenderType block, @Nullable RenderType entity) {
  public static final RenderTypeGroup EMPTY = new RenderTypeGroup(null, null);

  public boolean isEmpty() {
    return block == null && entity == null;
  }
}
