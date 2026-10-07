package slimeknights.mantle.platform.fluid;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

import javax.annotation.Nullable;
import java.util.Objects;

/**
 * Fabric replacement for NeoForge's {@code FluidStack}. Amounts are in millibuckets (1000 per bucket) like NeoForge;
 * conversion to Fabric droplets (81 per mB) happens at the Transfer API boundary.
 */
public final class FluidStack implements net.minecraft.core.component.DataComponentHolder {
  public static final FluidStack EMPTY = new FluidStack(Fluids.EMPTY, 0);

  public static final Codec<Fluid> FLUID_NON_EMPTY_CODEC = BuiltInRegistries.FLUID.byNameCodec().validate(
    fluid -> fluid == Fluids.EMPTY ? com.mojang.serialization.DataResult.error(() -> "Fluid must not be minecraft:empty") : com.mojang.serialization.DataResult.success(fluid));

  private static final Codec<FluidStack> FULL_CODEC = RecordCodecBuilder.create(i -> i.group(
    FLUID_NON_EMPTY_CODEC.fieldOf("id").forGetter(FluidStack::getFluid),
    ExtraCodecs.POSITIVE_INT.fieldOf("amount").forGetter(FluidStack::getAmount),
    DataComponentPatch.CODEC.optionalFieldOf("components", DataComponentPatch.EMPTY).forGetter(s -> s.components.asPatch())
  ).apply(i, FluidStack::new));
  public static final Codec<FluidStack> CODEC = Codec.lazyInitialized(() -> FULL_CODEC);
  public static final Codec<FluidStack> OPTIONAL_CODEC = ExtraCodecs.optionalEmptyMap(CODEC).xmap(
    opt -> opt.orElse(EMPTY), stack -> stack.isEmpty() ? java.util.Optional.empty() : java.util.Optional.of(stack));
  public static final MapCodec<FluidStack> MAP_CODEC = MapCodec.assumeMapUnsafe(CODEC);

  public static final StreamCodec<RegistryFriendlyByteBuf,FluidStack> OPTIONAL_STREAM_CODEC = new StreamCodec<>() {
    @Override
    public FluidStack decode(RegistryFriendlyByteBuf buf) {
      int amount = buf.readVarInt();
      if (amount <= 0) {
        return EMPTY;
      }
      Fluid fluid = ByteBufCodecs.registry(net.minecraft.core.registries.Registries.FLUID).decode(buf);
      DataComponentPatch patch = DataComponentPatch.STREAM_CODEC.decode(buf);
      return new FluidStack(fluid, amount, patch);
    }

    @Override
    public void encode(RegistryFriendlyByteBuf buf, FluidStack stack) {
      if (stack.isEmpty()) {
        buf.writeVarInt(0);
      } else {
        buf.writeVarInt(stack.getAmount());
        ByteBufCodecs.registry(net.minecraft.core.registries.Registries.FLUID).encode(buf, stack.getFluid());
        DataComponentPatch.STREAM_CODEC.encode(buf, stack.components.asPatch());
      }
    }
  };
  public static final StreamCodec<RegistryFriendlyByteBuf,FluidStack> STREAM_CODEC = new StreamCodec<>() {
    @Override
    public FluidStack decode(RegistryFriendlyByteBuf buf) {
      FluidStack stack = OPTIONAL_STREAM_CODEC.decode(buf);
      if (stack.isEmpty()) {
        throw new io.netty.handler.codec.DecoderException("Empty FluidStack not allowed");
      }
      return stack;
    }

    @Override
    public void encode(RegistryFriendlyByteBuf buf, FluidStack stack) {
      if (stack.isEmpty()) {
        throw new io.netty.handler.codec.EncoderException("Empty FluidStack not allowed");
      }
      OPTIONAL_STREAM_CODEC.encode(buf, stack);
    }
  };

  private final Fluid fluid;
  private int amount;
  private final PatchedDataComponentMap components;

  public FluidStack(Fluid fluid, int amount) {
    this(fluid, amount, new PatchedDataComponentMap(DataComponentMap.EMPTY));
  }

  public FluidStack(Holder<Fluid> fluid, int amount) {
    this(fluid.value(), amount);
  }

  public FluidStack(Fluid fluid, int amount, DataComponentPatch patch) {
    this(fluid, amount, PatchedDataComponentMap.fromPatch(DataComponentMap.EMPTY, patch));
  }

  private FluidStack(Fluid fluid, int amount, PatchedDataComponentMap components) {
    this.fluid = fluid;
    this.amount = amount;
    this.components = components;
  }

  public Fluid getFluid() {
    return isEmpty() ? Fluids.EMPTY : fluid;
  }

  /** Gets the fluid regardless of whether the stack is empty */
  public Fluid getFluidHolder() {
    return fluid;
  }

  public boolean isEmpty() {
    return fluid == Fluids.EMPTY || amount <= 0;
  }

  public int getAmount() {
    return isEmpty() ? 0 : amount;
  }

  public void setAmount(int amount) {
    this.amount = amount;
  }

  public void grow(int amount) {
    this.amount += amount;
  }

  public void shrink(int amount) {
    this.amount -= amount;
  }

  public FluidStack copy() {
    return isEmpty() ? EMPTY : new FluidStack(fluid, amount, components.copy());
  }

  public FluidStack copyWithAmount(int amount) {
    if (isEmpty()) {
      return EMPTY;
    }
    FluidStack copy = copy();
    copy.setAmount(amount);
    return copy;
  }

  public boolean is(Fluid other) {
    return getFluid() == other;
  }

  public boolean is(TagKey<Fluid> tag) {
    return getFluid().is(tag);
  }

  public Component getHoverName() {
    return getFluid().defaultFluidState().createLegacyBlock().getBlock().getName();
  }

  /** Gets a data component */
  @Nullable
  public <T> T get(DataComponentType<? extends T> type) {
    return components.get(type);
  }

  public <T> T getOrDefault(DataComponentType<? extends T> type, T fallback) {
    return components.getOrDefault(type, fallback);
  }

  @Nullable
  public <T> T set(DataComponentType<? super T> type, @Nullable T value) {
    return components.set(type, value);
  }

  @Nullable
  public <T> T remove(DataComponentType<? extends T> type) {
    return components.remove(type);
  }

  @Override
  public DataComponentMap getComponents() {
    return components;
  }

  /** Checks if this stack has no component changes */
  public boolean isComponentsPatchEmpty() {
    return components.asPatch().isEmpty();
  }

  /** Applies component changes to this stack */
  public void applyComponents(DataComponentPatch patch) {
    components.applyPatch(patch);
  }

  public DataComponentPatch getComponentsPatch() {
    return components.asPatch();
  }

  /** Checks if the fluid and components match, ignoring amount */
  public boolean isSameFluidSameComponents(FluidStack other) {
    return is(other.getFluid()) && Objects.equals(components, other.components);
  }

  /** Checks if the fluid matches, ignoring components and amount */
  public boolean isSameFluid(FluidStack other) {
    return is(other.getFluid());
  }

  /** Checks if two stacks have the same fluid and components, ignoring amount (NeoForge name) */
  public static boolean isSameFluidSameComponents(FluidStack a, FluidStack b) {
    if (a == b) return true;
    return a.isSameFluidSameComponents(b);
  }

  /** Checks if both stacks match, including amount */
  public static boolean matches(FluidStack a, FluidStack b) {
    return a.getAmount() == b.getAmount() && isSameFluidSameComponents(a, b);
  }

  @Override
  public boolean equals(Object o) {
    return o instanceof FluidStack other && matches(this, other);
  }

  @Override
  public int hashCode() {
    return Objects.hash(getFluid(), getAmount(), components);
  }

  @Override
  public String toString() {
    return getAmount() + " " + BuiltInRegistries.FLUID.getKey(getFluid());
  }

  /** Saves this stack to a tag, 1.20 style */
  public net.minecraft.nbt.Tag save(net.minecraft.core.HolderLookup.Provider registries) {
    return CODEC.encodeStart(registries.createSerializationContext(net.minecraft.nbt.NbtOps.INSTANCE), this).getOrThrow();
  }

  /** Saves this stack to a tag using the global registries */
  public net.minecraft.nbt.Tag save() {
    return save(slimeknights.mantle.util.GlobalRegistries.get());
  }

  /** Parses a stack from a tag using the global registries, returning empty if invalid */
  public static FluidStack parse(net.minecraft.nbt.Tag tag) {
    return parse(slimeknights.mantle.util.GlobalRegistries.get(), tag);
  }

  /** Parses a stack from a tag, returning empty if invalid */
  public static FluidStack parse(net.minecraft.core.HolderLookup.Provider registries, net.minecraft.nbt.Tag tag) {
    return CODEC.parse(registries.createSerializationContext(net.minecraft.nbt.NbtOps.INSTANCE), tag).result().orElse(EMPTY);
  }
}
