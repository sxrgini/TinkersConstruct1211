package slimeknights.mantle.recipe.ingredient;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import slimeknights.mantle.data.loadable.Loadable;
import slimeknights.mantle.platform.fluid.FluidStack;
import slimeknights.mantle.util.typed.TypedMap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

/**
 * Fluid ingredient with an amount, using the Mantle 1.20 style API that Tinkers' Construct is written against.
 * Matches by fluid and ignores stack data. For the NeoForge style ingredient see {@link slimeknights.mantle.platform.fluid.crafting.FluidIngredient}.
 */
public abstract class FluidIngredient implements Predicate<FluidStack> {
  /** Ingredient that matches nothing */
  public static final FluidIngredient EMPTY = new Empty();
  /** Loadable for fluid ingredients, empty arrays and the empty ingredient are allowed */
  public static final Loadable<FluidIngredient> LOADABLE = new IngredientLoadable();

  /* Creation */

  /** Creates an ingredient matching a single fluid with the given amount */
  public static FluidIngredient of(Fluid fluid, int amount) {
    return new FluidMatch(fluid, amount);
  }

  /** Creates an ingredient matching a fluid tag with the given amount */
  public static FluidIngredient of(TagKey<Fluid> tag, int amount) {
    return new TagMatch(tag, amount);
  }

  /** Creates an ingredient matching the fluid and amount of the stack */
  public static FluidIngredient of(FluidStack stack) {
    return of(stack.getFluid(), stack.getAmount());
  }

  /** Creates an ingredient matching any of the passed ingredients */
  public static FluidIngredient of(FluidIngredient... ingredients) {
    return of(List.of(ingredients));
  }

  /** Creates an ingredient matching any of the passed ingredients */
  public static FluidIngredient of(Collection<FluidIngredient> ingredients) {
    return switch (ingredients.size()) {
      case 0 -> EMPTY;
      case 1 -> ingredients.iterator().next();
      default -> new Compound(List.copyOf(ingredients));
    };
  }


  /* Matching */

  /** Checks if the fluid matches this ingredient, ignoring amount */
  public abstract boolean test(Fluid fluid);

  @Override
  public boolean test(FluidStack stack) {
    return test(stack.getFluid());
  }

  /** Gets the amount needed for the given fluid, which should match this ingredient */
  public abstract int getAmount(Fluid fluid);

  /** Gets a list of stacks matching this ingredient, with amounts. May contain several stacks for tags and compounds */
  public abstract List<FluidStack> getFluids();

  /** Checks if this ingredient matches nothing */
  public boolean isEmpty() {
    return getFluids().isEmpty();
  }

  /** Serializes this ingredient to JSON */
  public abstract JsonElement serialize();


  /* Implementations */

  private static class Empty extends FluidIngredient {
    @Override
    public boolean test(Fluid fluid) {
      return false;
    }

    @Override
    public int getAmount(Fluid fluid) {
      return 0;
    }

    @Override
    public List<FluidStack> getFluids() {
      return Collections.emptyList();
    }

    @Override
    public JsonElement serialize() {
      return new JsonArray();
    }
  }

  /** Creates an ingredient matching a fluid by registry name, which loads even if the fluid does not exist. Used for compat with other mods. */
  public static FluidIngredient ofName(ResourceLocation name, int amount) {
    return BuiltInRegistries.FLUID.getOptional(name).map(fluid -> of(fluid, amount)).orElseGet(() -> new NameMatch(name, amount));
  }

  /** Ingredient for a fluid that is not registered, never matches anything */
  private static class NameMatch extends FluidIngredient {
    private final ResourceLocation name;
    private final int amount;

    private NameMatch(ResourceLocation name, int amount) {
      this.name = name;
      this.amount = amount;
    }

    @Override
    public boolean test(Fluid fluid) {
      return false;
    }

    @Override
    public int getAmount(Fluid fluid) {
      return amount;
    }

    @Override
    public List<FluidStack> getFluids() {
      return Collections.emptyList();
    }

    @Override
    public JsonElement serialize() {
      JsonObject json = new JsonObject();
      json.addProperty("name", name.toString());
      json.addProperty("amount", amount);
      return json;
    }
  }

  private static class FluidMatch extends FluidIngredient {
    private final Fluid fluid;
    private final int amount;
    private List<FluidStack> fluids;

    private FluidMatch(Fluid fluid, int amount) {
      this.fluid = fluid;
      this.amount = amount;
    }

    @Override
    public boolean test(Fluid fluid) {
      return this.fluid == fluid;
    }

    @Override
    public int getAmount(Fluid fluid) {
      return amount;
    }

    @Override
    public List<FluidStack> getFluids() {
      if (fluids == null) {
        fluids = List.of(new FluidStack(fluid, amount));
      }
      return fluids;
    }

    @Override
    public JsonElement serialize() {
      JsonObject json = new JsonObject();
      json.addProperty("name", BuiltInRegistries.FLUID.getKey(fluid).toString());
      json.addProperty("amount", amount);
      return json;
    }
  }

  private static class TagMatch extends FluidIngredient {
    private final TagKey<Fluid> tag;
    private final int amount;
    private List<FluidStack> fluids;

    private TagMatch(TagKey<Fluid> tag, int amount) {
      this.tag = tag;
      this.amount = amount;
    }

    @Override
    public boolean test(Fluid fluid) {
      return fluid.is(tag);
    }

    @Override
    public int getAmount(Fluid fluid) {
      return amount;
    }

    @Override
    public List<FluidStack> getFluids() {
      if (fluids == null) {
        List<FluidStack> stacks = new ArrayList<>();
        for (Fluid fluid : BuiltInRegistries.FLUID) {
          if (fluid != Fluids.EMPTY && fluid.defaultFluidState().isSource() && fluid.is(tag)) {
            stacks.add(new FluidStack(fluid, amount));
          }
        }
        fluids = List.copyOf(stacks);
      }
      return fluids;
    }

    @Override
    public JsonElement serialize() {
      JsonObject json = new JsonObject();
      json.addProperty("tag", tag.location().toString());
      json.addProperty("amount", amount);
      return json;
    }
  }

  private static class Compound extends FluidIngredient {
    private final List<FluidIngredient> children;
    private List<FluidStack> fluids;

    private Compound(List<FluidIngredient> children) {
      this.children = children;
    }

    @Override
    public boolean test(Fluid fluid) {
      for (FluidIngredient child : children) {
        if (child.test(fluid)) {
          return true;
        }
      }
      return false;
    }

    @Override
    public int getAmount(Fluid fluid) {
      for (FluidIngredient child : children) {
        if (child.test(fluid)) {
          return child.getAmount(fluid);
        }
      }
      return 0;
    }

    @Override
    public List<FluidStack> getFluids() {
      if (fluids == null) {
        List<FluidStack> stacks = new ArrayList<>();
        for (FluidIngredient child : children) {
          stacks.addAll(child.getFluids());
        }
        fluids = List.copyOf(stacks);
      }
      return fluids;
    }

    @Override
    public JsonElement serialize() {
      JsonArray array = new JsonArray();
      for (FluidIngredient child : children) {
        array.add(child.serialize());
      }
      return array;
    }
  }

  /** Loadable implementation, networked as a list of simple ingredients */
  private static class IngredientLoadable implements Loadable<FluidIngredient> {
    @Override
    public FluidIngredient convert(JsonElement element, String key, TypedMap context) {
      if (element.isJsonArray()) {
        List<FluidIngredient> list = new ArrayList<>();
        for (JsonElement child : element.getAsJsonArray()) {
          list.add(convert(child, key, context));
        }
        return of(list);
      }
      JsonObject json = GsonHelper.convertToJsonObject(element, key);
      int amount = GsonHelper.getAsInt(json, "amount");
      if (json.has("name")) {
        ResourceLocation name = ResourceLocation.parse(GsonHelper.getAsString(json, "name"));
        return ofName(name, amount);
      }
      if (json.has("tag")) {
        return of(TagKey.create(Registries.FLUID, ResourceLocation.parse(GsonHelper.getAsString(json, "tag"))), amount);
      }
      throw new JsonSyntaxException("Fluid ingredient must have name or tag");
    }

    @Override
    public JsonElement serialize(FluidIngredient object, TypedMap context) {
      return object.serialize();
    }

    @Override
    public FluidIngredient decode(RegistryFriendlyByteBuf buffer, TypedMap context) {
      int size = buffer.readVarInt();
      List<FluidIngredient> list = new ArrayList<>(size);
      for (int i = 0; i < size; i++) {
        if (buffer.readBoolean()) {
          list.add(of(TagKey.create(Registries.FLUID, buffer.readResourceLocation()), buffer.readVarInt()));
        } else {
          list.add(of(BuiltInRegistries.FLUID.get(buffer.readResourceLocation()), buffer.readVarInt()));
        }
      }
      return of(list);
    }

    @Override
    public void encode(RegistryFriendlyByteBuf buffer, FluidIngredient value, TypedMap context) {
      List<FluidIngredient> simple = new ArrayList<>();
      flatten(value, simple);
      buffer.writeVarInt(simple.size());
      for (FluidIngredient ingredient : simple) {
        if (ingredient instanceof TagMatch tag) {
          buffer.writeBoolean(true);
          buffer.writeResourceLocation(tag.tag.location());
          buffer.writeVarInt(tag.amount);
        } else {
          FluidMatch match = (FluidMatch) ingredient;
          buffer.writeBoolean(false);
          buffer.writeResourceLocation(BuiltInRegistries.FLUID.getKey(match.fluid));
          buffer.writeVarInt(match.amount);
        }
      }
    }

    private static void flatten(FluidIngredient ingredient, List<FluidIngredient> out) {
      if (ingredient instanceof Compound compound) {
        compound.children.forEach(child -> flatten(child, out));
      } else if (!(ingredient instanceof Empty) && !(ingredient instanceof NameMatch)) {
        out.add(ingredient);
      }
    }
  }
}
