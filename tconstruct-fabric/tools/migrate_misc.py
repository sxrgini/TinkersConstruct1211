#!/usr/bin/env python3
"""Maps assorted Forge classes onto Mantle shims or Fabric API."""
import re, pathlib
root = pathlib.Path("src/main/java")
P = "slimeknights.mantle.platform."
imports = {
  "net.minecraftforge.event.TickEvent.PlayerTickEvent": P + "event.TickEvent.PlayerTickEvent",
  "net.minecraftforge.event.TickEvent.Phase": P + "event.TickEvent.Phase",
  "net.minecraftforge.event.TickEvent": P + "event.TickEvent",
  "net.minecraftforge.fml.LogicalSide": P + "event.LogicalSide",
  "net.minecraftforge.common.util.INBTSerializable": P + "util.INBTSerializable",
  "net.minecraftforge.energy.IEnergyStorage": P + "capability.IEnergyStorage",
  "net.minecraftforge.common.IForgeShearable": P + "item.IShearable",
  "net.minecraftforge.common.SoundActions": P + "fluid.SoundActions",
  "net.minecraftforge.common.util.FakePlayer": "net.fabricmc.fabric.api.entity.FakePlayer",
  "net.minecraftforge.common.util.TablePrinter": P + "util.TablePrinter",
  "net.minecraftforge.common.loot.IGlobalLootModifier": P + "loot.IGlobalLootModifier",
  "net.minecraftforge.common.loot.LootModifier": P + "loot.LootModifier",
  "net.minecraftforge.common.loot.LootModifierManager": P + "loot.GlobalLootModifierManager",
}
n = 0
for f in root.rglob("*.java"):
  s = o = f.read_text()
  for a, b in imports.items():
    s = s.replace("import %s;" % a, "import %s;" % b)
  s = s.replace("IForgeShearable", "IShearable")
  s = re.sub(r"FMLEnvironment\.dist\s*==\s*Dist\.CLIENT", "FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT", s)
  if "FabricLoader.getInstance().getEnvironmentType()" in s:
    for imp in ("net.fabricmc.loader.api.FabricLoader", "net.fabricmc.api.EnvType"):
      if "import %s;" % imp not in s:
        s = re.sub(r"(package [^\n]*\n\n)", r"\1import %s;\n" % imp, s, count=1)
    s = s.replace("import net.minecraftforge.fml.loading.FMLEnvironment;\n", "")
  if s != o:
    f.write_text(s); n += 1
print(n)
