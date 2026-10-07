package slimeknights.mantle.platform.client;

import java.util.function.Consumer;

/** Implemented by mob effects that supply client extensions, collected by {@link ClientExtensions#init()} */
public interface IClientMobEffectExtensionsProvider {
  void initializeClient(Consumer<IClientMobEffectExtensions> consumer);
}
