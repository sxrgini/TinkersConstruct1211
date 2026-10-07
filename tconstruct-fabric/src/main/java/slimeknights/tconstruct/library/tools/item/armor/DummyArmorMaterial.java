package slimeknights.tconstruct.library.tools.item.armor;

import lombok.Getter;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.crafting.Ingredient;
import slimeknights.mantle.registration.object.IdAwareObject;

import java.util.List;
import java.util.Map;

/** Armor material that returns 0 except for name, since we bypass all the usages */
public class DummyArmorMaterial implements IdAwareObject {
  @Getter
  private final ResourceLocation id;
  /** The vanilla material backing this dummy */
  @Getter
  private final Holder<ArmorMaterial> holder;

  public DummyArmorMaterial(ResourceLocation id, Holder<SoundEvent> equipSound) {
    this.id = id;
    Map<ArmorItem.Type,Integer> defense = new java.util.EnumMap<>(ArmorItem.Type.class);
    for (ArmorItem.Type type : slimeknights.tconstruct.library.tools.definition.ModifiableArmorMaterial.ARMOR_TYPES) {
      defense.put(type, 0);
    }
    this.holder = Holder.direct(new ArmorMaterial(defense, 0, equipSound, () -> Ingredient.EMPTY, List.of(new ArmorMaterial.Layer(id)), 0f, 0f));
  }
}
