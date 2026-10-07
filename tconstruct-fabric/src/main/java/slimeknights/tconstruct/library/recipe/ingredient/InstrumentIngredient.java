package slimeknights.tconstruct.library.recipe.ingredient;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Instrument;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.Nullable;
import slimeknights.mantle.platform.ingredient.ICustomIngredient;
import slimeknights.mantle.platform.ingredient.IngredientType;
import slimeknights.tconstruct.TConstruct;

import java.util.Optional;
import java.util.stream.Stream;

/** Ingredient matching an {@link net.minecraft.world.item.InstrumentItem} with a particular instrument. */
public class InstrumentIngredient implements ICustomIngredient {
  public static final ResourceLocation ID = TConstruct.getResource("instrument");
  public static final MapCodec<InstrumentIngredient> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
    BuiltInRegistries.ITEM.byNameCodec().fieldOf("item").forGetter(i -> i.item),
    ResourceKey.codec(Registries.INSTRUMENT).optionalFieldOf("instrument").forGetter(i -> Optional.ofNullable(i.instrument)),
    TagKey.codec(Registries.INSTRUMENT).optionalFieldOf("ignore").forGetter(i -> Optional.ofNullable(i.ignore))
  ).apply(instance, (item, instrument, ignore) -> {
    if (instrument.isEmpty() == ignore.isEmpty()) {
      throw new IllegalArgumentException("Invalid InstrumentIngredient: must set either 'instrument' or 'ignore'");
    }
    return new InstrumentIngredient(item, instrument.orElse(null), ignore.orElse(null));
  }));
  public static final StreamCodec<RegistryFriendlyByteBuf,InstrumentIngredient> STREAM_CODEC = StreamCodec.of(
    (buffer, ingredient) -> {
      ByteBufCodecs.registry(Registries.ITEM).encode(buffer, ingredient.item);
      if (ingredient.instrument != null) {
        buffer.writeBoolean(true);
        buffer.writeResourceLocation(ingredient.instrument.location());
      } else {
        assert ingredient.ignore != null;
        buffer.writeBoolean(false);
        buffer.writeResourceLocation(ingredient.ignore.location());
      }
    },
    buffer -> {
      Item item = ByteBufCodecs.registry(Registries.ITEM).decode(buffer);
      if (buffer.readBoolean()) {
        return new InstrumentIngredient(item, ResourceKey.create(Registries.INSTRUMENT, buffer.readResourceLocation()), null);
      }
      return new InstrumentIngredient(item, null, TagKey.create(Registries.INSTRUMENT, buffer.readResourceLocation()));
    });
  public static final IngredientType<InstrumentIngredient> TYPE = new IngredientType<>(CODEC, STREAM_CODEC);

  private final Item item;
  @Nullable
  private final ResourceKey<Instrument> instrument;
  @Nullable
  private final TagKey<Instrument> ignore;

  protected InstrumentIngredient(Item item, @Nullable ResourceKey<Instrument> instrument, @Nullable TagKey<Instrument> ignore) {
    this.item = item;
    this.instrument = instrument;
    this.ignore = ignore;
  }

  /** Creates a new instance matching the given instrument */
  public static Ingredient of(ItemLike item, ResourceKey<Instrument> instrument) {
    return new InstrumentIngredient(item.asItem(), instrument, null).toVanilla();
  }

  /** Creates a new instance ignoring the given tag */
  public static Ingredient of(ItemLike item, TagKey<Instrument> ignore) {
    return new InstrumentIngredient(item.asItem(), null, ignore).toVanilla();
  }

  @Override
  public boolean isSimple() {
    return false;
  }

  @Override
  public boolean test(ItemStack stack) {
    if (!stack.is(item)) {
      return false;
    }
    Holder<Instrument> held = stack.get(DataComponents.INSTRUMENT);
    if (held != null) {
      // if we just want the instrument to match, can just compare by key
      if (this.instrument != null) {
        return held.is(this.instrument);
      }
      assert this.ignore != null;
      // must not be in the tag
      return !held.is(ignore);
    }
    // if no instrument, its fine as long as we don't have a specific instrument
    return this.instrument == null;
  }

  @Override
  public Stream<ItemStack> getItems() {
    ItemStack stack = new ItemStack(item);
    // set the instrument on the stack
    if (instrument != null) {
      BuiltInRegistries.INSTRUMENT.getHolder(instrument).ifPresent(holder -> stack.set(DataComponents.INSTRUMENT, holder));
    }
    return Stream.of(stack);
  }

  @Override
  public IngredientType<?> getType() {
    return TYPE;
  }
}
