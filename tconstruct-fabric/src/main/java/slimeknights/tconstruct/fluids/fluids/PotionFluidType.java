package slimeknights.tconstruct.fluids.fluids;

import slimeknights.tconstruct.library.utils.StackNbt;
import slimeknights.tconstruct.library.utils.PotionHelper;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.Potions;
import slimeknights.mantle.platform.client.IClientFluidTypeExtensions;
import slimeknights.mantle.platform.fluid.FluidStack;
import slimeknights.mantle.platform.fluid.FluidType;
import slimeknights.mantle.fluid.texture.ClientTextureFluidType;
import slimeknights.mantle.recipe.helper.FluidOutput;
import slimeknights.tconstruct.fluids.TinkerFluids;

import java.util.Objects;
import java.util.function.Consumer;

public class PotionFluidType extends FluidType {
  /** 1.20 had an explicit empty potion, 1.21 does not */
  private static final ResourceKey<Potion> EMPTY_ID = ResourceKey.create(net.minecraft.core.registries.Registries.POTION, ResourceLocation.withDefaultNamespace("empty"));

  public PotionFluidType(Properties properties) {
    super(properties);
  }

  public String getDescriptionId(FluidStack stack) {
    return PotionHelper.getName(stack, "item.minecraft.potion.effect.");
  }

  public ItemStack getBucket(FluidStack fluidStack) {
    ItemStack itemStack = new ItemStack(fluidStack.getFluid().getBucket());
    itemStack.applyComponents(fluidStack.getComponentsPatch());
    return itemStack;
  }

  @Override
  public void initializeClient(Consumer<IClientFluidTypeExtensions> consumer) {
    consumer.accept(new ClientTextureFluidType(this) {
      /**
       * Gets the color, based on {@link PotionHelper#getColor}
       * @param stack  Fluid stack instance
       * @return  Color for the fluid
       */
      @Override
      public int getTintColor(FluidStack stack) {
        if (PotionHelper.isEmpty(stack)) {
          return getTintColor();
        }
        return PotionHelper.getColor(stack) | 0xFF000000;
      }
    });
  }

  /** Creates the potion tag */
  private static CompoundTag potionTag(ResourceLocation location) {
    CompoundTag tag = new CompoundTag();
    tag.putString("Potion", location.toString());
    return tag;
  }

  /** Creates a fluid stack for the given potion */
  public static FluidStack potionFluid(ResourceKey<Potion> potion, int size) {
    CompoundTag tag = null;
    if (!EMPTY_ID.equals(potion)) {
      tag = potionTag(potion.location());
    }
    return new FluidStack(TinkerFluids.potion.get(), size, tag);
  }

  /** Creates a fluid stack for the given potion */
  @SuppressWarnings("deprecation")  // forge registries have nullable keys, like why would you want that?
  public static FluidStack potionFluid(Potion potion, int size) {
    CompoundTag tag = null;
    if (!BuiltInRegistries.POTION.getKey(potion).equals(EMPTY_ID.location())) {
      tag = potionTag(BuiltInRegistries.POTION.getKey(potion));
    }
    return new FluidStack(TinkerFluids.potion.get(), size, tag);
  }

  /** Creates a fluid output for the given potion */
  @SuppressWarnings("deprecation")  // forge registries have nullable keys, like why would you want that?
  public static FluidOutput potionResult(Potion potion, int size) {
    CompoundTag tag = null;
    if (!BuiltInRegistries.POTION.getKey(potion).equals(EMPTY_ID.location())) {
      tag = potionTag(BuiltInRegistries.POTION.getKey(potion));
    }
    return FluidOutput.fromTag(Objects.requireNonNull(TinkerFluids.potion.getCommonTag()), size, tag);
  }

  /** Creates a potion bucket for the given potion */
  public static ItemStack potionBucket(ResourceKey<Potion> potion) {
    ItemStack stack = new ItemStack(TinkerFluids.potion);
    if (!EMPTY_ID.equals(potion)) {
      StackNbt.setTag(stack, potionTag(potion.location()));
    }
    return stack;
  }

  /** Creates a potion bucket for the given potion */
  @SuppressWarnings("deprecation")  // forge registries have nullable keys, like why would you want that?
  public static ItemStack potionBucket(Potion potion) {
    ItemStack stack = new ItemStack(TinkerFluids.potion);
    if (!BuiltInRegistries.POTION.getKey(potion).equals(EMPTY_ID.location())) {
      StackNbt.setTag(stack, potionTag(BuiltInRegistries.POTION.getKey(potion)));
    }
    return stack;
  }
}
