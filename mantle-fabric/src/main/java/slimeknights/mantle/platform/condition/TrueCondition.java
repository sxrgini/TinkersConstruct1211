package slimeknights.mantle.platform.condition;

import com.mojang.serialization.MapCodec;

/** Condition that is always true */
public final class TrueCondition implements ICondition {
  public static final TrueCondition INSTANCE = new TrueCondition();
  public static final MapCodec<TrueCondition> CODEC = MapCodec.unit(INSTANCE);

  private TrueCondition() {}

  @Override
  public MapCodec<? extends ICondition> codec() {
    return CODEC;
  }

  @Override
  public boolean test(IContext context) {
    return true;
  }
}
