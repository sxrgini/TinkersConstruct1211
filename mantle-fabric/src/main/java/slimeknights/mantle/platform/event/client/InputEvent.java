package slimeknights.mantle.platform.event.client;

import net.minecraft.client.KeyMapping;
import net.minecraft.world.InteractionHand;
import slimeknights.mantle.platform.event.Event;

/** Input events, fired by {@code MinecraftMixin} */
public abstract class InputEvent extends Event {
  /** Fired when an attack, use item or pick block key triggers an interaction. Canceling stops the interaction. */
  @Cancelable
  public static class InteractionKeyMappingTriggered extends InputEvent {
    private final int button;
    private final KeyMapping keyMapping;
    private final InteractionHand hand;
    private boolean swingHand = true;

    public InteractionKeyMappingTriggered(int button, KeyMapping keyMapping, InteractionHand hand) {
      this.button = button;
      this.keyMapping = keyMapping;
      this.hand = hand;
    }

    public int getButton() { return button; }
    public KeyMapping getKeyMapping() { return keyMapping; }
    public InteractionHand getHand() { return hand; }
    public boolean shouldSwingHand() { return swingHand; }
    public void setSwingHand(boolean swingHand) { this.swingHand = swingHand; }
    public boolean isAttack() { return button == 0; }
    public boolean isUseItem() { return button == 1; }
    public boolean isPickBlock() { return button == 2; }
  }
}
