#!/usr/bin/env python3
"""Single item and block registrations return DeferredItem/DeferredBlock in Mantle 1.21, retype the fields."""
import re, pathlib
root = pathlib.Path("src/main/java")
n = 0
for f in root.rglob("*.java"):
  s = o = f.read_text()
  s = re.sub(r"ItemObject<Item,\s*([\w<>?, ]+?)>(\s+\w+\s*=\s*ITEMS\.register\()", r"DeferredItem<\1>\2", s)
  s = re.sub(r"ItemObject<Block,\s*([\w<>?, ]+?)>(\s+\w+\s*=\s*BLOCKS\.register\()", r"DeferredBlock<\1>\2", s)
  for cls in ("DeferredItem", "DeferredBlock"):
    if cls + "<" in s and "import slimeknights.mantle.platform.registry.%s;" % cls not in s:
      s = re.sub(r"(package [^\n]*\n\n)", r"\1import slimeknights.mantle.platform.registry.%s;\n" % cls, s, count=1)
  if s != o:
    f.write_text(s); n += 1
print(n)
