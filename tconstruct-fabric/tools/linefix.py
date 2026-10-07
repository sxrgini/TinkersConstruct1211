"""Applies regex substitutions only on lines javac reported with matching errors"""
import re,sys,collections
log=open('/tmp/claude-0/tc.log').read().split("\n")
def balanced(s,i):
  d=0;j=i
  while j<len(s):
    if s[j]=="(":d+=1
    elif s[j]==")":
      d-=1
      if d==0:return j
    j+=1
  return -1
def split_top(s):
  args=[];d=0;cur=""
  for c in s:
    if c in "([{<" and c!="<":d+=1
    if c in ")]}":d-=1
    if c=="," and d==0: args.append(cur.strip());cur=""
    else: cur+=c
  args.append(cur.strip());return args
def fluid_ctor(line):
  # new FluidStack(FluidStack, int) -> A.copyWithAmount(B)
  out=line;pos=0
  while True:
    i=out.find("new FluidStack(",pos)
    if i<0:return out
    st=i+len("new FluidStack(");e=balanced(out,st-1)
    args=split_top(out[st:e])
    if len(args)==2:
      a,b=args
      out=out[:i]+"%s.copyWithAmount(%s)"%(a,b)+out[e+1:]
      pos=i
    else: pos=e
RULES=[
 (r"no suitable constructor found for FluidStack\(FluidStack,int\)",fluid_ctor),
 (r"cannot find symbol",None),
]
def stackrem(line):
  return line.replace(".getCraftingRemainingItem()",".getRecipeRemainder()")
fixes=collections.defaultdict(set)
for i,l in enumerate(log):
  m=re.match(r"(/\S+\.java):(\d+): error: (.*)",l)
  if not m: continue
  msg=m.group(3)
  if re.search(RULES[0][0],msg): fixes[m.group(1)].add((int(m.group(2)),"fluid"))
  elif msg=="cannot find symbol" and i+3<len(log) and "method getCraftingRemainingItem()" in " ".join(log[i+1:i+5]):
    fixes[m.group(1)].add((int(m.group(2)),"rem"))
n=0
for f,items in fixes.items():
  lines=open(f).read().split("\n")
  for ln,kind in items:
    l=lines[ln-1]
    l2=fluid_ctor(l) if kind=="fluid" else stackrem(l)
    if l2!=l: lines[ln-1]=l2;n+=1
  open(f,"w").write("\n".join(lines))
print("linefix",n)
