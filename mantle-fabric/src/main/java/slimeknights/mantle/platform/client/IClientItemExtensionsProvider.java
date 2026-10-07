package slimeknights.mantle.platform.client;

import java.util.function.Consumer;

/** Implemented by items that supply client extensions, collected by {@link ClientExtensions#init()} */
public interface IClientItemExtensionsProvider {
  void initializeClient(Consumer<IClientItemExtensions> consumer);
}
