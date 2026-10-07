package slimeknights.mantle.platform.fluid;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.Rarity;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

/**
 * Describes the shared attributes of a fluid, replacing NeoForge's {@code FluidType}. Fabric has no equivalent, so this is registered in {@link FluidTypes#REGISTRY}
 * and linked to fluids through {@link FluidTypeProvider}.
 */
public class FluidType {
  /** Volume of a bucket in millibuckets */
  public static final int BUCKET_VOLUME = 1000;

  private final Properties properties;

  public FluidType(Properties properties) {
    this.properties = properties;
  }

  /** Density, negative means lighter than air */
  public int getDensity() {
    return properties.density;
  }

  public int getTemperature() {
    return properties.temperature;
  }

  public int getViscosity() {
    return properties.viscosity;
  }

  public int getLightLevel() {
    return properties.lightLevel;
  }

  /** Gets the light level for the given stack */
  public int getLightLevel(FluidStack stack) {
    return properties.lightLevel;
  }

  public boolean isLighterThanAir() {
    return properties.density <= 0;
  }

  public Rarity getRarity() {
    return properties.rarity;
  }

  public boolean canDrown() {
    return properties.canDrown;
  }

  public boolean canExtinguish() {
    return properties.canExtinguish;
  }

  public double getMotionScale() {
    return properties.motionScale;
  }

  /** Checks if the entity drowns in this fluid */
  public boolean canDrownIn(net.minecraft.world.entity.LivingEntity entity) {
    return properties.canDrown;
  }

  /** Hook for client extensions, called by the client initializer */
  public void initializeClient(java.util.function.Consumer<slimeknights.mantle.platform.client.IClientFluidTypeExtensions> consumer) {}

  public boolean canSwim() {
    return properties.canSwim;
  }

  public boolean canConvertToSource() {
    return properties.canConvertToSource;
  }

  /** Gets the translation key for the fluid, null if not set */
  @Nullable
  public String getDescriptionId() {
    return properties.descriptionId;
  }

  /** Gets the sound for the given action, or null if unset */
  @Nullable
  public SoundEvent getSound(SoundAction action) {
    return properties.sounds.get(action);
  }

  /** Gets the sound for the given action and stack, or null if unset */
  @Nullable
  public SoundEvent getSound(FluidStack stack, SoundAction action) {
    return getSound(action);
  }

  /** Properties builder for a fluid type */
  public static class Properties {
    private String descriptionId;
    private int density = 1000;
    private int temperature = 300;
    private int viscosity = 1000;
    private int lightLevel = 0;
    private Rarity rarity = Rarity.COMMON;
    private boolean canSwim = true;
    private boolean canConvertToSource = false;
    private boolean canDrown = true;
    private boolean canExtinguish = false;
    private double motionScale = 0.014;
    private final Map<SoundAction,SoundEvent> sounds = new HashMap<>();

    private Properties() {}

    public static Properties create() {
      return new Properties();
    }

    public Properties descriptionId(String id) {
      this.descriptionId = id;
      return this;
    }

    public Properties density(int density) {
      this.density = density;
      return this;
    }

    public Properties temperature(int temperature) {
      this.temperature = temperature;
      return this;
    }

    public Properties viscosity(int viscosity) {
      this.viscosity = viscosity;
      return this;
    }

    public Properties lightLevel(int lightLevel) {
      this.lightLevel = lightLevel;
      return this;
    }

    public Properties rarity(Rarity rarity) {
      this.rarity = rarity;
      return this;
    }

    public Properties canSwim(boolean canSwim) {
      this.canSwim = canSwim;
      return this;
    }

    public Properties canConvertToSource(boolean canConvertToSource) {
      this.canConvertToSource = canConvertToSource;
      return this;
    }

    public Properties canDrown(boolean canDrown) {
      this.canDrown = canDrown;
      return this;
    }

    public Properties canExtinguish(boolean canExtinguish) {
      this.canExtinguish = canExtinguish;
      return this;
    }

    public Properties motionScale(double motionScale) {
      this.motionScale = motionScale;
      return this;
    }

    /** Path type is not used on Fabric, kept so Forge style definitions compile */
    public Properties pathType(net.minecraft.world.level.pathfinder.PathType type) {
      return this;
    }

    /** Path type is not used on Fabric, kept so Forge style definitions compile */
    public Properties adjacentPathType(net.minecraft.world.level.pathfinder.PathType type) {
      return this;
    }

    public Properties sound(SoundAction action, SoundEvent sound) {
      this.sounds.put(action, sound);
      return this;
    }
  }

}
