package slimeknights.tconstruct.fluids;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import slimeknights.mantle.platform.event.AttachCapabilitiesEvent;
import slimeknights.mantle.platform.event.SubscribeEvent;
import slimeknights.mantle.platform.fluid.FluidStack;
import slimeknights.mantle.platform.fluid.FluidType;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.fluids.util.ConstantFluidContainerWrapper;

/**
 * Event subscriber for modifier events
 * Note the way the subscribers are set up, technically works on anything that has the tic_modifiers tag
 */
@SuppressWarnings("unused")
public class FluidEvents {
  @SubscribeEvent
  static void attachCapabilities(AttachCapabilitiesEvent<ItemStack> event) {
    ItemStack stack = event.getObject();
    if (event.getObject().is(Items.POWDER_SNOW_BUCKET)) {
      event.addCapability(
        TConstruct.getResource("powdered_snow"),
        new ConstantFluidContainerWrapper(new FluidStack(TinkerFluids.powderedSnow.get(), FluidType.BUCKET_VOLUME), stack, Items.BUCKET.getDefaultInstance()));
    }
  }
}
