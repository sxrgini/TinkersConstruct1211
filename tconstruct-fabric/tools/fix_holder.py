import re,sys,collections
log=open('/tmp/claude-0/tc.log').read().split("\n")
fix=collections.defaultdict(set)
for i,l in enumerate(log):
  m=re.match(r"(/\S+\.java):(\d+): error: cannot find symbol",l)
  if not m: continue
  blk=log[i+1:i+7]
  sym=next((x for x in blk if "symbol:" in x),None)
  loc=next((x for x in blk if "location:" in x),None)
  if not sym or not loc: continue
  ms=re.search(r"symbol:\s+method (\w+)\(",sym)
  if not ms: continue
  meth=ms.group(1)
  if re.search(r"(variable \w+ of type (Holder|Reference|Holder\.Reference|DeferredHolder|DeferredItem|DeferredBlock)<|interface Holder<|class Holder<)",loc):
    mv=re.search(r"variable (\w+) of type",loc)
    fix[m.group(1)].add((int(m.group(2)),meth,mv.group(1) if mv else None))
n=0
for f,items in fix.items():
  lines=open(f).read().split("\n")
  for ln,meth,var in items:
    l=lines[ln-1]
    if meth=="get":
      l2=re.sub(r"\b%s\.get\(\)"%re.escape(var),"%s.value()"%var,l) if var else l
    else:
      if var and re.search(r"\b%s\.%s\("%(var,meth),l):
        l2=re.sub(r"\b%s\.%s\("%(var,meth),"%s.value().%s("%(var,meth),l,count=1)
      else:
        l2=re.sub(r"(\))\.%s\("%meth,r"\1.value().%s("%meth,l,count=1)
    if l2!=l: lines[ln-1]=l2;n+=1
  open(f,"w").write("\n".join(lines))
print("holder fixes",n)
