package slimeknights.tconstruct.fluids.fluids;

import slimeknights.mantle.fluid.texture.TextureFluidType;
import slimeknights.mantle.platform.fluid.FluidType.Properties;
import net.minecraft.world.entity.LivingEntity;
import slimeknights.mantle.platform.client.IClientFluidTypeExtensions;
import slimeknights.mantle.fluid.texture.ClientInvertedFluidType;
import slimeknights.tconstruct.common.TinkerTags;

import java.util.function.Consumer;

/** Fluid Type that does not affect slimes */
public class SlimeFluidType extends TextureFluidType {
  public SlimeFluidType(Properties properties) {
    super(properties);
  }

  @Override
  public boolean canDrownIn(LivingEntity entity) {
    return !entity.getType().is(TinkerTags.EntityTypes.SLIMES);
  }

  public static class Inverted extends SlimeFluidType {
    public Inverted(Properties properties) {
      super(properties);
    }

    @Override
    public void initializeClient(Consumer<IClientFluidTypeExtensions> consumer) {
      consumer.accept(new ClientInvertedFluidType(this));
    }
  }
}
