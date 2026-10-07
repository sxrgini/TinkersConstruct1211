package slimeknights.mantle.fluid.texture;

import slimeknights.mantle.platform.client.IClientFluidTypeExtensions;
import slimeknights.mantle.platform.fluid.FluidType;

import java.util.function.Consumer;

/** Fluid type that fetches its textures from the fluid texture manager, see {@link ClientTextureFluidType} */
public class TextureFluidType extends FluidType {
  public TextureFluidType(Properties properties) {
    super(properties);
  }

  @Override
  public void initializeClient(Consumer<IClientFluidTypeExtensions> consumer) {
    consumer.accept(new ClientTextureFluidType(this));
  }
}
