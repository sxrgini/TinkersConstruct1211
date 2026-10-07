package slimeknights.mantle.platform.condition;

import com.mojang.serialization.MapCodec;

/** Condition that is always false */
public final class FalseCondition implements ICondition {
  public static final FalseCondition INSTANCE = new FalseCondition();
  public static final MapCodec<FalseCondition> CODEC = MapCodec.unit(INSTANCE);

  private FalseCondition() {}

  @Override
  public MapCodec<? extends ICondition> codec() {
    return CODEC;
  }

  @Override
  public boolean test(IContext context) {
    return false;
  }
}
