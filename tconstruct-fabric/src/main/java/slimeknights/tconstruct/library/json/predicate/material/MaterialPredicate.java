package slimeknights.tconstruct.library.json.predicate.material;

import slimeknights.mantle.util.typed.TypedMap;
import slimeknights.mantle.data.loadable.field.ContextKey;
import slimeknights.mantle.data.JsonCodec;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Codec;
import com.google.gson.JsonElement;
import net.minecraft.tags.TagKey;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.mantle.data.loadable.record.SingletonLoader;
import slimeknights.mantle.data.predicate.IJsonPredicate;
import slimeknights.mantle.data.predicate.TagPredicateRegistry;
import slimeknights.tconstruct.library.json.TinkerLoadables;
import slimeknights.tconstruct.library.materials.MaterialRegistry;
import slimeknights.tconstruct.library.materials.definition.IMaterial;
import slimeknights.tconstruct.library.materials.definition.MaterialVariantId;
import slimeknights.tconstruct.library.recipe.casting.material.MaterialCastingLookup;

import java.util.List;
import java.util.function.Predicate;

/** Predicate that checks against a material variant */
public interface MaterialPredicate extends IJsonPredicate<MaterialVariantId> {
  /** Instance that always returns true */
  MaterialPredicate ANY = simple(material -> true);
  /** Instance that always returns false */
  MaterialPredicate NONE = simple(material -> false);
  /** Loader for material predicates */
  TagPredicateRegistry<IMaterial,MaterialVariantId> LOADER = new TagPredicateRegistry<>("Material Predicate", ANY, NONE, TinkerLoadables.MATERIAL_TAGS, (tag, source) -> MaterialRegistry.getInstance().isInTag(source.getId(), tag));

  /** Codec that reads a bare string as an exact material variant, and otherwise a full predicate */
  Codec<IJsonPredicate<MaterialVariantId>> CODEC = new JsonCodec<>() {
    @Override
    public IJsonPredicate<MaterialVariantId> deserialize(JsonElement element, DynamicOps<?> ops) {
      if (element.isJsonPrimitive()) {
        return variant(MaterialVariantId.LOADABLE.convert(element, "material", TypedMap.EMPTY));
      }
      return LOADER.convert(element, "material", TypedMap.EMPTY);
    }

    @Override
    public JsonElement serialize(IJsonPredicate<MaterialVariantId> object, DynamicOps<?> ops, TypedMap context) {
      return LOADER.serialize(object);
    }
  };
  /** Stream codec for the predicate */
  StreamCodec<RegistryFriendlyByteBuf,IJsonPredicate<MaterialVariantId>> STREAM_CODEC = LOADER.asStreamCodec();

  /** Gets an inverted condition */
  @Override
  default IJsonPredicate<MaterialVariantId> inverted() {
    return LOADER.invert(this);
  }

  @Override
  RecordLoadable<? extends MaterialPredicate> getLoader();


  /* Singleton */

  /** Matches any materials that have a casting recipe */
  MaterialPredicate CASTABLE = simple(material -> !MaterialCastingLookup.getCastingFluids(material).isEmpty());
  /** Matches any materials that have a composite recipe */
  MaterialPredicate COMPOSITE = simple(material -> !MaterialCastingLookup.getCompositeFluids(material).isEmpty());

  /** Creates a new simple predicate */
  static MaterialPredicate simple(Predicate<MaterialVariantId> predicate) {
    return SingletonLoader.singleton(loader -> new MaterialPredicate() {
      @Override
      public boolean matches(MaterialVariantId tool) {
        return predicate.test(tool);
      }

      @Override
      public RecordLoadable<? extends MaterialPredicate> getLoader() {
        return loader;
      }
    });
  }


  /* Helper methods */

  /** Creates a classic style predicate from a material, handling exact ID and the default variant. */
  static MaterialPredicate variant(MaterialVariantId material) {
    String variant = material.getVariant();
    if (variant.isEmpty()) {
      return new MaterialIdPredicate(material.getId());
    }
    // default variant means exact match on ID
    if (MaterialVariantId.DEFAULT_VARIANT.equals(variant)) {
      return new MaterialVariantPredicate(material.getId());
    }
    // anything else is exact match on material
    return new MaterialVariantPredicate(material);
  }

  /** Creates a tag predicate */
  static IJsonPredicate<MaterialVariantId> tag(TagKey<IMaterial> tag) {
    return LOADER.tag(tag);
  }

  /** Creates an and predicate */
  @SafeVarargs
  static IJsonPredicate<MaterialVariantId> and(IJsonPredicate<MaterialVariantId>... predicates) {
    return LOADER.and(List.of(predicates));
  }

  /** Creates an or predicate */
  @SafeVarargs
  static IJsonPredicate<MaterialVariantId> or(IJsonPredicate<MaterialVariantId>... predicates) {
    return LOADER.or(List.of(predicates));
  }
}
