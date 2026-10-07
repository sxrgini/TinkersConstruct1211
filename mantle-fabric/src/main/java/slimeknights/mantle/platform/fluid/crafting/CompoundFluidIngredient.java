package slimeknights.mantle.platform.fluid.crafting;

import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import slimeknights.mantle.platform.fluid.FluidStack;

import java.util.List;
import java.util.stream.Stream;

/** Ingredient matching any of its children. Only exists in list form in JSON, so the type codec is only used on the network. */
public final class CompoundFluidIngredient extends FluidIngredient {
  public static final MapCodec<CompoundFluidIngredient> CODEC = MapCodec.unit(() -> new CompoundFluidIngredient(List.of()));
  public static final StreamCodec<RegistryFriendlyByteBuf,CompoundFluidIngredient> STREAM_CODEC = FluidIngredient.STREAM_CODEC.apply(net.minecraft.network.codec.ByteBufCodecs.list())
    .map(CompoundFluidIngredient::new, CompoundFluidIngredient::children);
  static final FluidIngredientType<CompoundFluidIngredient> TYPE = new FluidIngredientType<>(CODEC, STREAM_CODEC);
  static {
    TYPE.register(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("mantle", "compound"));
  }

  private final List<FluidIngredient> children;

  private CompoundFluidIngredient(List<FluidIngredient> children) {
    this.children = children;
  }

  /** Creates a compound ingredient, unwrapping if there is only one child */
  public static FluidIngredient of(List<FluidIngredient> children) {
    return children.size() == 1 ? children.get(0) : new CompoundFluidIngredient(List.copyOf(children));
  }

  public List<FluidIngredient> children() {
    return children;
  }

  @Override
  public FluidIngredientType<?> getType() {
    return TYPE;
  }

  @Override
  public boolean test(FluidStack stack) {
    return children.stream().anyMatch(child -> child.test(stack));
  }

  @Override
  public boolean isSimple() {
    return children.stream().allMatch(FluidIngredient::isSimple);
  }

  @Override
  protected Stream<FluidStack> generateStacks() {
    return children.stream().flatMap(child -> child.getStacks().stream());
  }

  @Override
  public boolean equals(Object obj) {
    return obj instanceof CompoundFluidIngredient other && other.children.equals(children);
  }

  @Override
  public int hashCode() {
    return children.hashCode();
  }
}
