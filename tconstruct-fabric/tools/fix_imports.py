#!/usr/bin/env python3
"""Adds imports for 'cannot find symbol: class X' errors in a javac log when X resolves uniquely in TC, Mantle or vanilla."""
import re, sys, subprocess, pathlib, collections, os, functools
log = pathlib.Path(sys.argv[1]).read_text()
JAR = os.environ.get("MC_JAR", "/root/.gradle/caches/fabric-loom/minecraftMaven/net/minecraft/minecraft-merged/1.21.1-loom.mappings.1_21_1.layered+hash.730628366-v2/minecraft-merged-1.21.1-loom.mappings.1_21_1.layered+hash.730628366-v2.jar")
roots = [pathlib.Path("src/main/java"), pathlib.Path("../mantle-fabric/src/main/java")]

@functools.lru_cache(None)
def vanilla():
  out = subprocess.run(["unzip", "-Z1", JAR], capture_output=True, text=True).stdout.split("\n")
  idx = collections.defaultdict(list)
  for l in out:
    if l.endswith(".class") and l.startswith("net/minecraft/") and "$" not in l.split("/")[-1]:
      idx[l.split("/")[-1][:-6]].append(l[:-6].replace("/", "."))
  return idx

@functools.lru_cache(None)
def sources():
  idx = collections.defaultdict(list)
  for r in roots:
    for f in r.rglob("*.java"):
      if "/data/" in str(f) or "/plugin/" in str(f): continue
      rel = str(f.relative_to(r))[:-5].replace("/", ".")
      idx[f.stem].append(rel)
  return idx

def resolve(name):
  s = sources().get(name, [])
  s = [c for c in s if c.startswith("slimeknights.")]
  tc = [c for c in s if c.startswith("slimeknights.tconstruct.")]
  if len(tc) == 1: return tc[0]
  if len(s) == 1: return s[0]
  v = vanilla().get(name, [])
  if len(v) > 1:
    nc = [c for c in v if ".client." not in c and ".data." not in c and ".server." not in c and ".gametest." not in c]
    if len(nc) == 1: v = nc
  if len(v) == 1: return v[0]
  return None

pat = re.compile(r"^(/[^\n]*?\.java):\d+: error: cannot find symbol\n[^\n]*\n[^\n]*\n\s*symbol:\s+class (\w+)", re.M)
wanted = collections.defaultdict(set)
for m in pat.finditer(log):
  wanted[m.group(1)].add(m.group(2))
added = 0
unresolved = collections.Counter()
for f, names in wanted.items():
  p = pathlib.Path(f)
  s = p.read_text()
  for name in sorted(names):
    if re.search(r"^import [\w.]*\.%s;" % name, s, re.M): continue
    target = resolve(name)
    if not target:
      unresolved[name] += 1
      continue
    s = re.sub(r"(package [^\n]*\n\n?)", r"\1import %s;\n" % target, s, count=1)
    added += 1
  p.write_text(s)
print("added", added)
print("unresolved:", unresolved.most_common(40))
