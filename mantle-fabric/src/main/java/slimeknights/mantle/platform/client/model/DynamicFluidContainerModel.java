package slimeknights.mantle.platform.client.model;

/** Helpers from Forge's dynamic fluid container model. Render types are not used on Fabric so these are empty. */
public final class DynamicFluidContainerModel {
  private DynamicFluidContainerModel() {}

  /** Gets the render types for the fluid layer, always empty on Fabric */
  public static RenderTypeGroup getLayerRenderTypes(boolean unlit) {
    return RenderTypeGroup.EMPTY;
  }
}
