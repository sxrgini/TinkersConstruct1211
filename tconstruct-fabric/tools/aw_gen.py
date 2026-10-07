#!/usr/bin/env python3
"""Generates access widener lines from a spec file using the mapped Minecraft jar.

Spec lines: <directive> <Class or Outer$Inner or simple name> <member name|*|<init>> [descriptor substring]
directive: accessible | extendable | mutable | accessible+mutable
Class may be a simple name (searched in the jar). Fields and every matching method overload are emitted.
"""
import subprocess, sys, re, os, functools
JAR = os.environ.get("MC_JAR", "/root/.gradle/caches/fabric-loom/minecraftMaven/net/minecraft/minecraft-merged/1.21.1-loom.mappings.1_21_1.layered+hash.730628366-v2/minecraft-merged-1.21.1-loom.mappings.1_21_1.layered+hash.730628366-v2.jar")

@functools.lru_cache(None)
def classes():
  out = subprocess.run(["unzip", "-Z1", JAR], capture_output=True, text=True).stdout.split("\n")
  return [l[:-6] for l in out if l.endswith(".class") and l.startswith("net/minecraft/")]

def find(cls):
  if "/" in cls or "." in cls:
    return cls.replace(".", "/")
  simple = cls
  matches = [c for c in classes() if c.split("/")[-1] == simple]
  if len(matches) != 1:
    raise SystemExit("class %s matches %s" % (cls, matches))
  return matches[0]

@functools.lru_cache(None)
def javap(cls):
  return subprocess.run(["javap", "-cp", JAR, "-p", "-s", cls.replace("/", ".")], capture_output=True, text=True).stdout

def members(cls):
  lines = javap(cls).split("\n")
  res = []
  i = 0
  while i < len(lines):
    l = lines[i].strip()
    if l.endswith(";") and i + 1 < len(lines) and lines[i + 1].strip().startswith("descriptor:"):
      desc = lines[i + 1].split("descriptor:")[1].strip()
      if "(" in l:
        m = re.search(r"([\w$<>]+)\(", l)
        name = m.group(1)
        # constructors print the class name
        if name == cls.split("/")[-1].replace("$", ".").split(".")[-1] or name == cls.replace("/", ".").replace("$", ".").split(".")[-1]:
          name = "<init>"
        res.append(("method", name, desc, l))
      else:
        name = l.rstrip(";").split()[-1]
        res.append(("field", name, desc, l))
      i += 2
    else:
      i += 1
  return res

out = []
for line in open(sys.argv[1]):
  line = line.strip()
  if not line or line.startswith("#"):
    if line.startswith("#"):
      out.append(line)
    continue
  parts = line.split()
  directive, cls, member = parts[0], find(parts[1]), parts[2]
  sub = parts[3] if len(parts) > 3 else None
  if member == "class":
    for d in directive.split("+"):
      out.append("%s class %s" % (d, cls))
    continue
  found = [m for m in members(cls) if (member == "*" or m[1] == member) and (sub is None or sub in m[2])]
  if not found:
    out.append("# MISSING %s" % line)
    continue
  for kind, name, desc, _ in found:
    for d in directive.split("+"):
      if d == "mutable" and kind == "method":
        continue
      out.append("%s %s %s %s %s" % (d, kind, cls, name, desc))
print("\n".join(out))
