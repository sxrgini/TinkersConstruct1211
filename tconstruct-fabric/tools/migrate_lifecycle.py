#!/usr/bin/env python3
"""Moves Forge lifecycle events and the mod bus onto the Mantle shim."""
import re, sys, pathlib

root = pathlib.Path(sys.argv[1] if len(sys.argv) > 1 else "src/main/java")
LC = "slimeknights.mantle.platform.event.lifecycle."
imports = {
  "net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent": LC + "FMLCommonSetupEvent",
  "net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent": LC + "FMLClientSetupEvent",
  "net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent": LC + "FMLLoadCompleteEvent",
  "net.minecraftforge.eventbus.api.IEventBus": "slimeknights.mantle.platform.event.EventBus",
}
changed = 0
for f in root.rglob("*.java"):
  s = o = f.read_text()
  for a, b in imports.items():
    s = s.replace("import " + a + ";", "import " + b + ";")
  s = re.sub(r"\bIEventBus\b", "EventBus", s)
  s = s.replace("FMLJavaModLoadingContext.get().getModEventBus()", "EventBus.MOD_BUS")
  s = s.replace("import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;\n", "")
  if s != o:
    f.write_text(s); changed += 1
print("changed", changed)
