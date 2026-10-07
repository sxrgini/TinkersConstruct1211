package slimeknights.mantle.platform.condition;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.loader.api.FabricLoader;

/** Condition that is true when the given mod is loaded */
public record ModLoadedCondition(String modid) implements ICondition {
  public static final MapCodec<ModLoadedCondition> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
    com.mojang.serialization.Codec.STRING.fieldOf("modid").forGetter(ModLoadedCondition::modid)
  ).apply(i, ModLoadedCondition::new));

  @Override
  public MapCodec<? extends ICondition> codec() {
    return CODEC;
  }

  @Override
  public boolean test(IContext context) {
    return FabricLoader.getInstance().isModLoaded(modid);
  }
}
