package slimeknights.tconstruct.mixin;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagEntry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Predicate;

/**
 * Data generation only: vanilla rejects tags referencing tags it did not generate itself, which includes the common tags provided by other mods.
 * NeoForge relaxes this using its existing file helper, so we do the same when the datagen flag is set.
 */
@Mixin(TagEntry.class)
public class TagEntryMixin {
  @Inject(method = "verifyIfPresent", at = @At("HEAD"), cancellable = true)
  private void tconstruct$skipVerification(Predicate<ResourceLocation> elementCheck, Predicate<ResourceLocation> tagCheck, CallbackInfoReturnable<Boolean> cir) {
    if (System.getProperty("fabric-api.datagen") != null) {
      cir.setReturnValue(true);
    }
  }
}
