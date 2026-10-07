package slimeknights.tconstruct.plugin.jsonthings;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.api.EnvType;

/** This plugin is referenced in the main class, so it may not directly access JSON Things classes. It may access classes that access them however */
public class JsonThingsPlugin {
  /** Called by mod constructor to register JsonThings things */
  public static void onConstruct() {
    FlexBlockTypes.init();
    FlexItemTypes.init();

    if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
      PluginClient.init();
    }
  }
}
