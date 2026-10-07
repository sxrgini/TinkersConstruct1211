package slimeknights.mantle.platform.fluid.crafting;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import slimeknights.mantle.platform.fluid.FluidStack;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * Fluid ingredient, replacing NeoForge's {@code FluidIngredient}. Supports the simple {@code fluid} and {@code tag} forms without a type,
 * typed forms using {@code type}, and lists of ingredients as a compound.
 */
public abstract class FluidIngredient implements Predicate<FluidStack> {
  private static final Map<ResourceLocation,FluidIngredientType<?>> TYPES = new HashMap<>();

  /** Registers a type, called by {@link FluidIngredientType#register} */
  static void registerType(ResourceLocation id, FluidIngredientType<?> type) {
    if (TYPES.putIfAbsent(id, type) != null) {
      throw new IllegalArgumentException("Duplicate fluid ingredient type " + id);
    }
  }

  /** Map codec handling typed and untyped single ingredients */
  public static final MapCodec<FluidIngredient> MAP_CODEC_NON_EMPTY = new MapCodec<>() {
    @Override
    public <T> Stream<T> keys(DynamicOps<T> ops) {
      return Stream.of("type", "fluid", "tag").map(ops::createString);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public <T> DataResult<FluidIngredient> decode(DynamicOps<T> ops, MapLike<T> input) {
      T type = input.get("type");
      if (type != null) {
        return ResourceLocation.CODEC.parse(ops, type).flatMap(id -> {
          FluidIngredientType<?> ingredientType = TYPES.get(id);
          if (ingredientType == null) {
            return DataResult.error(() -> "Unknown fluid ingredient type " + id);
          }
          return ((MapCodec) ingredientType.codec()).decode(ops, input).map(i -> (FluidIngredient) i);
        });
      }
      if (input.get("fluid") != null) {
        return SingleFluidIngredient.CODEC.decode(ops, input).map(i -> (FluidIngredient) i);
      }
      if (input.get("tag") != null) {
        return TagFluidIngredient.CODEC.decode(ops, input).map(i -> (FluidIngredient) i);
      }
      return DataResult.error(() -> "Fluid ingredient must have one of type, fluid or tag");
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public <T> RecordBuilder<T> encode(FluidIngredient input, DynamicOps<T> ops, RecordBuilder<T> prefix) {
      FluidIngredientType type = input.getType();
      prefix.add("type", ResourceLocation.CODEC.encodeStart(ops, type.id()));
      return type.codec().encode(input, ops, prefix);
    }
  };

  /** Codec allowing a single ingredient or a list of ingredients */
  public static final Codec<FluidIngredient> CODEC_NON_EMPTY = Codec.lazyInitialized(() -> Codec.either(
    MAP_CODEC_NON_EMPTY.codec(),
    FluidIngredient.CODEC_NON_EMPTY.listOf()
  ).xmap(
    either -> either.map(single -> single, CompoundFluidIngredient::of),
    ingredient -> ingredient instanceof CompoundFluidIngredient compound ? com.mojang.datafixers.util.Either.right(compound.children()) : com.mojang.datafixers.util.Either.left(ingredient)
  ));

  /** Same as {@link #CODEC_NON_EMPTY}, but also allows the empty ingredient */
  public static final Codec<FluidIngredient> CODEC = CODEC_NON_EMPTY;

  public static final StreamCodec<RegistryFriendlyByteBuf,FluidIngredient> STREAM_CODEC = new StreamCodec<>() {
    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public FluidIngredient decode(RegistryFriendlyByteBuf buf) {
      ResourceLocation id = buf.readResourceLocation();
      FluidIngredientType<?> type = TYPES.get(id);
      if (type == null) {
        throw new IllegalStateException("Unknown fluid ingredient type " + id);
      }
      return (FluidIngredient) ((StreamCodec) type.streamCodec()).decode(buf);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public void encode(RegistryFriendlyByteBuf buf, FluidIngredient ingredient) {
      FluidIngredientType type = ingredient.getType();
      buf.writeResourceLocation(type.id());
      type.streamCodec().encode(buf, ingredient);
    }
  };

  @Nullable
  private List<FluidStack> stacks;

  /** Gets the type for serialization */
  public abstract FluidIngredientType<?> getType();

  /** Checks if the stack matches, ignoring amount */
  @Override
  public abstract boolean test(FluidStack stack);

  /** If true, matching only depends on the fluid, not any stack data */
  public abstract boolean isSimple();

  /** Generates the list of matching stacks, each with an amount of 1 bucket */
  protected abstract Stream<FluidStack> generateStacks();

  /** Gets a list of all matching stacks, cached */
  public final List<FluidStack> getStacks() {
    if (stacks == null) {
      stacks = generateStacks().toList();
    }
    return stacks;
  }

  /** Checks if this ingredient matches nothing */
  public boolean isEmpty() {
    return getStacks().isEmpty();
  }

  /** Creates an ingredient matching the given tag */
  public static FluidIngredient tag(TagKey<Fluid> tag) {
    return new TagFluidIngredient(tag);
  }

  /** Creates an ingredient matching a single fluid */
  public static FluidIngredient single(Fluid fluid) {
    return new SingleFluidIngredient(fluid);
  }

  /** Creates an ingredient matching the fluid of the stack */
  public static FluidIngredient single(FluidStack stack) {
    return new SingleFluidIngredient(stack.getFluid());
  }

  /** Creates an ingredient matching any of the passed ingredients */
  public static FluidIngredient of(FluidIngredient... ingredients) {
    return CompoundFluidIngredient.of(List.of(ingredients));
  }

  /** Creates an ingredient matching nothing */
  public static FluidIngredient empty() {
    return CompoundFluidIngredient.of(new ArrayList<>());
  }

  // register built in types
  static {
    SingleFluidIngredient.TYPE.register(ResourceLocation.fromNamespaceAndPath("mantle", "single"));
    TagFluidIngredient.TYPE.register(ResourceLocation.fromNamespaceAndPath("mantle", "tag"));
  }

  /** Helper so Fluids isn't flagged unused */
  static boolean isEmptyFluid(Fluid fluid) {
    return fluid == Fluids.EMPTY || BuiltInRegistries.FLUID.getKey(fluid) == null;
  }
}
