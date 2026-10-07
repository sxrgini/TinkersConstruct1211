package slimeknights.mantle.platform.capability;

import slimeknights.mantle.platform.fluid.IFluidHandler;
import slimeknights.mantle.platform.fluid.IFluidHandlerItem;
import slimeknights.mantle.platform.item.IItemHandler;

/** Built in capabilities, replacing Forge's {@code ForgeCapabilities} */
public final class Capabilities {
  public static final Capability<IItemHandler> ITEM_HANDLER = new Capability<>("item_handler");
  public static final Capability<IFluidHandler> FLUID_HANDLER = new Capability<>("fluid_handler");
  public static final Capability<IFluidHandlerItem> FLUID_HANDLER_ITEM = new Capability<>("fluid_handler_item");

  private Capabilities() {}
}
