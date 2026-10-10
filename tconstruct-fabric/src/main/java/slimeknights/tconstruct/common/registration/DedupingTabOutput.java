package slimeknights.tconstruct.common.registration;

import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTab.TabVisibility;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackLinkedSet;

import java.util.Set;

/**
 * Creative tab output that tolerates adding the same stack more than once. Vanilla 1.21 throws on duplicates in the same tab,
 * where Forge allowed it. Repeated stacks are dropped, or only added to the part of the tab (search or main) that does not have them yet.
 */
public class DedupingTabOutput implements CreativeModeTab.Output {
  private final CreativeModeTab.Output delegate;
  private final Set<ItemStack> parent = ItemStackLinkedSet.createTypeAndComponentsSet();
  private final Set<ItemStack> search = ItemStackLinkedSet.createTypeAndComponentsSet();

  private DedupingTabOutput(CreativeModeTab.Output delegate) {
    this.delegate = delegate;
  }

  /** Wraps the output, use in the display items generator of a tab */
  public static CreativeModeTab.Output wrap(CreativeModeTab.Output output) {
    return output instanceof DedupingTabOutput ? output : new DedupingTabOutput(output);
  }

  @Override
  public void accept(ItemStack stack, TabVisibility visibility) {
    boolean inParent = visibility != TabVisibility.SEARCH_TAB_ONLY;
    boolean inSearch = visibility != TabVisibility.PARENT_TAB_ONLY;
    if (inParent && parent.contains(stack)) {
      inParent = false;
    }
    if (inSearch && search.contains(stack)) {
      inSearch = false;
    }
    if (!inParent && !inSearch) {
      return;
    }
    if (inParent) {
      parent.add(stack.copy());
    }
    if (inSearch) {
      search.add(stack.copy());
    }
    delegate.accept(stack, inParent && inSearch ? TabVisibility.PARENT_AND_SEARCH_TABS : inParent ? TabVisibility.PARENT_TAB_ONLY : TabVisibility.SEARCH_TAB_ONLY);
  }
}
