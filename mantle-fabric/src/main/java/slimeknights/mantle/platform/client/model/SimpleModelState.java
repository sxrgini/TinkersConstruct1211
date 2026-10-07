package slimeknights.mantle.platform.client.model;

import com.mojang.math.Transformation;
import net.minecraft.client.resources.model.ModelState;

/** Simple model state implementation */
public record SimpleModelState(Transformation rotation, boolean uvLocked) implements ModelState {
  @Override
  public Transformation getRotation() {
    return rotation;
  }

  @Override
  public boolean isUvLocked() {
    return uvLocked;
  }
}
