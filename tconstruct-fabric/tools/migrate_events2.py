#!/usr/bin/env python3
"""Maps Forge server/lifecycle events and small helpers to Mantle shims."""
import re, pathlib
root = pathlib.Path("src/main/java")
P = "slimeknights.mantle.platform.event."
imports = {
  "net.minecraftforge.event.AddReloadListenerEvent": P + "server.AddReloadListenerEvent",
  "net.minecraftforge.event.OnDatapackSyncEvent": P + "server.OnDatapackSyncEvent",
  "net.minecraftforge.client.event.RegisterClientReloadListenersEvent": P + "client.RegisterClientReloadListenersEvent",
  "net.minecraftforge.fml.ModLoader": P + "lifecycle.ModLoader",
  "net.minecraftforge.fml.event.IModBusEvent": P + "lifecycle.IModBusEvent",
}
n = 0
for f in root.rglob("*.java"):
  s = o = f.read_text()
  for a, b in imports.items():
    s = s.replace("import %s;" % a, "import %s;" % b)
  s = s.replace("import net.minecraftforge.common.ForgeI18n;\n", "import net.minecraft.locale.Language;\n")
  s = re.sub(r"ForgeI18n\.getPattern\(([^;]*?)\)", r"Language.getInstance().getOrDefault(\1)", s)
  if s != o:
    f.write_text(s); n += 1
print(n)
