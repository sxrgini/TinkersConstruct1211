package slimeknights.mantle.data.loadable.common;

import net.minecraft.world.item.crafting.Ingredient;

/** Loadables for ingredients, backed by the vanilla codecs which Fabric extends with custom ingredients. */
public final class IngredientLoadable {
  private IngredientLoadable() {}

  /** Ingredient that may be empty */
  public static final CodecLoadable<Ingredient> ALLOW_EMPTY = new CodecLoadable.Direct<>(Ingredient.CODEC, Ingredient.CONTENTS_STREAM_CODEC);
  /** Ingredient that must match at least one item */
  public static final CodecLoadable<Ingredient> DISALLOW_EMPTY = new CodecLoadable.Direct<>(Ingredient.CODEC_NONEMPTY, Ingredient.CONTENTS_STREAM_CODEC);
}
