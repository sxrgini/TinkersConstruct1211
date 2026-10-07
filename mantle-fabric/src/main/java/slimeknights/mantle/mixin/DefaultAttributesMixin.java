package slimeknights.mantle.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.DefaultAttributes;
import org.spongepowered.asm.mixin.Mixin;
import slimeknights.mantle.platform.event.entity.EntityAttributeModificationEvent;

/** Applies attributes added through {@link EntityAttributeModificationEvent} */
@Mixin(DefaultAttributes.class)
public class DefaultAttributesMixin {
  @WrapMethod(method = "getSupplier")
  private static AttributeSupplier mantle$modifySupplier(EntityType<? extends LivingEntity> type, Operation<AttributeSupplier> original) {
    return EntityAttributeModificationEvent.modify(type, original.call(type));
  }
}
