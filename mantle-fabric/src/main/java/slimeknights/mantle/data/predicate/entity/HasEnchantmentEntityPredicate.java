package slimeknights.mantle.data.predicate.entity;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import slimeknights.mantle.data.loadable.Loadables;
import slimeknights.mantle.data.loadable.record.RecordLoadable;

/**
 * Predicate that checks if the given entity has the given enchantment on any of their equipment.
 * @param enchantment  Enchantment
 */
public record HasEnchantmentEntityPredicate(ResourceKey<Enchantment> enchantment) implements LivingEntityPredicate {
  public static final RecordLoadable<HasEnchantmentEntityPredicate> LOADER = RecordLoadable.create(Loadables.ENCHANTMENT_KEY.requiredField("enchantment", HasEnchantmentEntityPredicate::enchantment), HasEnchantmentEntityPredicate::new);

  @Override
  public boolean matches(LivingEntity entity) {
    return entity.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).get(enchantment).map(holder -> EnchantmentHelper.getEnchantmentLevel(holder, entity) > 0).orElse(false);
  }

  @Override
  public RecordLoadable<HasEnchantmentEntityPredicate> getLoader() {
    return LOADER;
  }
}
