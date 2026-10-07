package slimeknights.mantle.platform.event.living;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/** Events for using an item, fired from mixins in {@code LivingEntity} */
public class LivingEntityUseItemEvent extends LivingEvent {
  private final ItemStack item;
  private int duration;

  public LivingEntityUseItemEvent(LivingEntity living, ItemStack item, int duration) {
    super(living);
    this.item = item;
    this.duration = duration;
  }

  public ItemStack getItem() {
    return item;
  }

  public int getDuration() {
    return duration;
  }

  public void setDuration(int duration) {
    this.duration = duration;
  }

  /** Fired when an item finishes being used, such as drinking a bottle */
  public static class Finish extends LivingEntityUseItemEvent {
    private ItemStack result;

    public Finish(LivingEntity living, ItemStack item, int duration, ItemStack result) {
      super(living, item, duration);
      this.result = result;
    }

    public ItemStack getResultStack() {
      return result;
    }

    public void setResultStack(ItemStack result) {
      this.result = result;
    }
  }
}
