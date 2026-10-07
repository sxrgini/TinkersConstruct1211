package slimeknights.mantle.platform.event;

/** Which side of the game a tick or event runs on, equivalent of Forge's LogicalSide */
public enum LogicalSide {
  CLIENT,
  SERVER;

  public boolean isClient() {
    return this == CLIENT;
  }

  public boolean isServer() {
    return this == SERVER;
  }
}
