package slimeknights.tconstruct.common.config;

import slimeknights.mantle.platform.config.ConfigSpec;
import slimeknights.mantle.platform.config.ConfigSpec.BooleanValue;

/** Config prop that runs a runnable assuming its true */
public class ConfigurableAction implements Runnable {
  private final BooleanValue prop;
  private final Runnable action;

  public ConfigurableAction(ConfigSpec.Builder builder, String name, boolean defaultValue, String comment, Runnable action) {
    prop = builder.comment(comment).worldRestart().define(name, defaultValue);
    this.action = action;
  }

  @Override
  public void run() {
    if (prop.get()) {
      action.run();
    }
  }
}
