package slimeknights.mantle.platform.condition;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;

/** Condition that is true when all children are true */
public record AndCondition(List<ICondition> values) implements ICondition {
  public static final MapCodec<AndCondition> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
    ICondition.CODEC.listOf().fieldOf("values").forGetter(AndCondition::values)
  ).apply(i, AndCondition::new));

  public AndCondition(ICondition... values) {
    this(List.of(values));
  }

  @Override
  public MapCodec<? extends ICondition> codec() {
    return CODEC;
  }

  @Override
  public boolean test(IContext context) {
    for (ICondition condition : values) {
      if (!condition.test(context)) {
        return false;
      }
    }
    return true;
  }
}
