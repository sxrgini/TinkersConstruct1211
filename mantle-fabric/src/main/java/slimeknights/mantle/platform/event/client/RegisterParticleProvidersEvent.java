package slimeknights.mantle.platform.event.client;

import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import slimeknights.mantle.platform.event.Event;
import slimeknights.mantle.platform.event.lifecycle.IModBusEvent;

/** Registers particle providers, backed by Fabric's particle factory registry */
public class RegisterParticleProvidersEvent extends Event implements IModBusEvent {
  /** Registers a provider that does not use a sprite set */
  public <T extends ParticleOptions> void registerSpecial(ParticleType<T> type, ParticleProvider<T> provider) {
    ParticleFactoryRegistry.getInstance().register(type, sprites -> provider);
  }

  /** Registers a provider that uses the sprite set from the particle JSON */
  public <T extends ParticleOptions> void registerSpriteSet(ParticleType<T> type, java.util.function.Function<net.minecraft.client.particle.SpriteSet,ParticleProvider<T>> registration) {
    ParticleFactoryRegistry.getInstance().register(type, registration::apply);
  }
}
