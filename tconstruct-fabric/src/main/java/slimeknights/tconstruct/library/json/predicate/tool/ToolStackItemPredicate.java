package slimeknights.tconstruct.library.json.predicate.tool;

import com.mojang.serialization.Codec;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.ItemSubPredicate;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import slimeknights.mantle.data.loadable.LoadableCodec;
import slimeknights.mantle.data.predicate.IJsonPredicate;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.common.TinkerTags.Items;
import slimeknights.tconstruct.library.tools.nbt.IToolContext;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;

/** Sub predicate for matching Tinker tools using a {@link ToolStackPredicate}, use {@link #toItemPredicate()} to create the full item predicate. */
public record ToolStackItemPredicate(IJsonPredicate<IToolStackView> predicate) implements ItemSubPredicate {
  public static final ResourceLocation ID = TConstruct.getResource("tool_stack");
  public static final Codec<ToolStackItemPredicate> CODEC = new LoadableCodec<>(ToolStackPredicate.LOADER).fieldOf("predicate")
    .xmap(ToolStackItemPredicate::new, ToolStackItemPredicate::predicate).codec();
  public static final ItemSubPredicate.Type<ToolStackItemPredicate> TYPE = new ItemSubPredicate.Type<>(CODEC);

  /** Creates a predicate matching the given tool predicate */
  public static ToolStackItemPredicate ofTool(IJsonPredicate<IToolStackView> predicate) {
    return new ToolStackItemPredicate(predicate);
  }

  /** Creates a predicate matching the given tool context predicate */
  public static ToolStackItemPredicate ofContext(IJsonPredicate<IToolContext> predicate) {
    return new ToolStackItemPredicate(ToolStackPredicate.context(predicate));
  }

  @Override
  public boolean matches(ItemStack stack) {
    // tag check is important to prevent accidentally modifying the data of non-tools
    return stack.is(Items.MODIFIABLE) && predicate.matches(ToolStack.from(stack));
  }

  /** Creates an item predicate using this sub predicate */
  public ItemPredicate toItemPredicate() {
    return ItemPredicate.Builder.item().withSubPredicate(TYPE, this).build();
  }
}
