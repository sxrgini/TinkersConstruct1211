package slimeknights.mantle.platform.condition;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;

/** Condition that is true when any child is true */
public record OrCondition(List<ICondition> values) implements ICondition {
  public static final MapCodec<OrCondition> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
    ICondition.CODEC.listOf().fieldOf("values").forGetter(OrCondition::values)
  ).apply(i, OrCondition::new));

  public OrCondition(ICondition... values) {
    this(List.of(values));
  }

  @Override
  public MapCodec<? extends ICondition> codec() {
    return CODEC;
  }

  @Override
  public boolean test(IContext context) {
    for (ICondition condition : values) {
      if (condition.test(context)) {
        return true;
      }
    }
    return false;
  }
}
