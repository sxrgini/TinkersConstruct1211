#!/usr/bin/env python3
"""Replaces @EventBusSubscriber annotations with explicit registration in TinkerEventSubscribers."""
import re, sys, pathlib
root = pathlib.Path("src/main/java")
entries = []
for f in sorted(root.rglob("*.java")):
  s = o = f.read_text()
  m = re.search(r"^@(?:Mod\.)?EventBusSubscriber\((.*?)\)\s*\n", s, re.M)
  if not m: continue
  args = m.group(1)
  mod = "bus" in args and re.search(r"bus\s*=\s*(?:Mod\.EventBusSubscriber\.)?Bus\.MOD", args) is not None
  client = "Dist.CLIENT" in args
  pkg = re.search(r"^package (.*);", s, re.M).group(1)
  entries.append((pkg + "." + f.stem, mod, client))
  s = s.replace(m.group(0), "")
  for imp in ["net.minecraftforge.fml.common.Mod.EventBusSubscriber", "net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus", "net.minecraftforge.fml.common.Mod"]:
    s = s.replace("import %s;\n" % imp, "")
  if not re.search(r"\bDist\.", s.replace("import net.minecraftforge.api.distmarker.Dist;", "")):
    s = s.replace("import net.minecraftforge.api.distmarker.Dist;\n", "")
  f.write_text(s)
out = ["package slimeknights.tconstruct;", "", "import slimeknights.mantle.platform.event.EventBus;", "", "/** Registers the static event subscriber classes, replacing Forge's annotation based discovery */", "final class TinkerEventSubscribers {", "  private TinkerEventSubscribers() {}", ""]
for name, hdr in (("registerCommon", lambda c: not c), ("registerClient", lambda c: c)):
  out.append("  static void %s() {" % name)
  for cls, mod, client in entries:
    if hdr(client):
      out.append("    EventBus.%s.register(%s.class);" % ("MOD_BUS" if mod else "BUS", cls))
  out += ["  }", ""]
out.append("}")
(root / "slimeknights/tconstruct/TinkerEventSubscribers.java").write_text("\n".join(out) + "\n")
print(len(entries))
