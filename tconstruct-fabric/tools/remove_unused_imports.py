#!/usr/bin/env python3
"""Removes imports of simple names that are never referenced, limited to the given package prefixes."""
import re, pathlib, sys
prefixes = sys.argv[1:] or ["net.minecraftforge."]
root = pathlib.Path("src/main/java")
n = 0
for f in root.rglob("*.java"):
  s = o = f.read_text()
  lines = s.split("\n")
  body = "\n".join(l for l in lines if not l.startswith("import "))
  keep = []
  for l in lines:
    m = re.match(r"import (static )?([\w.]+?)(?:\.\*)?;", l)
    if m and any(m.group(2).startswith(p) for p in prefixes) and not l.endswith(".*;"):
      simple = m.group(2).split(".")[-1]
      if not re.search(r"\b%s\b" % re.escape(simple), body):
        n += 1
        continue
    keep.append(l)
  s = "\n".join(keep)
  if s != o:
    f.write_text(s)
print("removed", n)
