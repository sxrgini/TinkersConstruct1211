package slimeknights.tconstruct.library.recipe.ingredient;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import slimeknights.mantle.data.loadable.Loadables;
import slimeknights.mantle.platform.ingredient.ICustomIngredient;
import slimeknights.mantle.platform.ingredient.IngredientType;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.common.TinkerTags;
import slimeknights.tconstruct.library.tools.definition.module.ToolHooks;
import slimeknights.tconstruct.library.module.ModuleHook;
import slimeknights.tconstruct.library.tools.item.IModifiable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/** Ingredient that only matches tools with a specific hook */
public class ToolHookIngredient implements ICustomIngredient {
  public static final ResourceLocation ID = TConstruct.getResource("tool_hook");
  public static final MapCodec<ToolHookIngredient> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
    TagKey.codec(Registries.ITEM).optionalFieldOf("tag", TinkerTags.Items.MODIFIABLE).forGetter(i -> i.tag),
    ToolHooks.LOADER.asCodec().fieldOf("hook").forGetter(i -> i.hook)
  ).apply(instance, ToolHookIngredient::new));
  public static final StreamCodec<RegistryFriendlyByteBuf,ToolHookIngredient> STREAM_CODEC = StreamCodec.composite(
    Loadables.ITEM_TAG.asStreamCodec(), i -> i.tag,
    ToolHooks.LOADER.asStreamCodec(), i -> i.hook,
    ToolHookIngredient::new);
  public static final IngredientType<ToolHookIngredient> TYPE = new IngredientType<>(CODEC, STREAM_CODEC);

  private final TagKey<Item> tag;
  private final ModuleHook<?> hook;

  protected ToolHookIngredient(TagKey<Item> tag, ModuleHook<?> hook) {
    this.tag = tag;
    this.hook = hook;
  }

  public static Ingredient of(TagKey<Item> tag, ModuleHook<?> hook) {
    return new ToolHookIngredient(tag, hook).toVanilla();
  }

  public static Ingredient of(ModuleHook<?> hook) {
    return of(TinkerTags.Items.MODIFIABLE, hook);
  }

  @Override
  public boolean test(ItemStack stack) {
    return stack.is(tag) && stack.getItem() instanceof IModifiable modifiable && modifiable.getToolDefinition().getData().getHooks().hasHook(hook);
  }

  @Override
  public boolean isSimple() {
    return true;
  }

  @SuppressWarnings("deprecation")
  @Override
  public Stream<ItemStack> getItems() {
    List<ItemStack> list = new ArrayList<>();
    // filtered version of tag values
    for (Holder<Item> holder : BuiltInRegistries.ITEM.getTagOrEmpty(tag)) {
      if (holder.value() instanceof IModifiable modifiable && modifiable.getToolDefinition().getData().getHooks().hasHook(hook)) {
        list.add(new ItemStack(modifiable));
      }
    }
    if (list.isEmpty()) {
      ItemStack barrier = new ItemStack(Items.BARRIER);
      barrier.set(net.minecraft.core.component.DataComponents.CUSTOM_NAME, Component.literal("Empty Tag: " + tag.location()));
      list.add(barrier);
    }
    return list.stream();
  }

  @Override
  public IngredientType<?> getType() {
    return TYPE;
  }
}
