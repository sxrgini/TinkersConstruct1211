#!/usr/bin/env python3
"""Moves @SubscribeEvent gatherData(GatherDataEvent) methods out of module classes, keeping their text for the datagen port."""
import re, pathlib
root = pathlib.Path("src/main/java")
out = pathlib.Path("docs/datagen")
for f in root.rglob("*.java"):
  s = f.read_text()
  m = re.search(r"\n(  @SubscribeEvent\n)?  (public |private |static )*void \w+\((final )?GatherDataEvent \w+\) \{", s)
  if not m: continue
  start = m.start() + 1
  i = s.index("{", m.start())
  depth = 0
  j = i
  while True:
    c = s[j]
    if c == "{": depth += 1
    elif c == "}":
      depth -= 1
      if depth == 0: break
    j += 1
  method = s[start:j + 1]
  (out / (f.stem + ".gatherData.txt")).write_text(method + "\n")
  s = s[:start] + s[j + 1:].lstrip("\n")
  s = s.replace("import net.minecraftforge.data.event.GatherDataEvent;\n", "")
  f.write_text(s)
  print("moved", f.name)
