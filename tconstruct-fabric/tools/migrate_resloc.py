#!/usr/bin/env python3
"""ResourceLocation constructors are private in 1.21."""
import re, pathlib
root = pathlib.Path("src/main/java")
n = 0
for f in root.rglob("*.java"):
  s = o = f.read_text()
  # two argument form: split on top level comma
  def two(m):
    return "ResourceLocation.fromNamespaceAndPath(" + m.group(1) + ")"
  s = re.sub(r"new ResourceLocation\(((?:[^(),]|\([^()]*\))+,(?:[^()]|\([^()]*\))+?)\)", two, s)
  s = re.sub(r"new ResourceLocation\(((?:[^()]|\([^()]*\))+?)\)", lambda m: "ResourceLocation.parse(" + m.group(1) + ")", s)
  if s != o:
    f.write_text(s); n += 1
print(n)
