package slimeknights.tconstruct.mixin;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import slimeknights.tconstruct.library.utils.StackNbt;

/** Deep copies custom data on stack copy, as we edit the live tag in place and the component would otherwise be shared between copies */
@Mixin(ItemStack.class)
public class ItemStackMixin {
  @Inject(method = "copy", at = @At("RETURN"))
  private void tconstruct$copyCustomData(CallbackInfoReturnable<ItemStack> cir) {
    ItemStack copy = cir.getReturnValue();
    CustomData data = copy.get(DataComponents.CUSTOM_DATA);
    if (data != null) {
      copy.set(DataComponents.CUSTOM_DATA, StackNbt.wrap(data.copyTag()));
    }
  }
}
