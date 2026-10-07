package slimeknights.mantle.platform;

import net.minecraft.core.Holder;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;

/** Attributes Forge added that vanilla 1.21 now provides under different names */
public final class PlatformAttributes {
  private PlatformAttributes() {}

  public static final Holder<Attribute> ENTITY_GRAVITY = Attributes.GRAVITY;
  public static final Holder<Attribute> BLOCK_REACH = Attributes.BLOCK_INTERACTION_RANGE;
  public static final Holder<Attribute> ENTITY_REACH = Attributes.ENTITY_INTERACTION_RANGE;
  public static final Holder<Attribute> STEP_HEIGHT_ADDITION = Attributes.STEP_HEIGHT;
  /** Closest vanilla equivalent of Forge's swim speed */
  public static final Holder<Attribute> SWIM_SPEED = Attributes.WATER_MOVEMENT_EFFICIENCY;
}
