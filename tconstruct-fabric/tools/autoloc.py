"""Append .location() to expressions javac reports as `<Id> cannot be converted to ResourceLocation`"""
import re,sys,collections
log=open(sys.argv[1]).read().split("\n")
IDS=("ModifierId","MaterialId","MaterialStatsId","ToolStatId","Pattern","ResourceId","SlotType")
fixes=collections.defaultdict(set)
i=0
while i<len(log):
  m=re.match(r"(/\S+\.java):(\d+): error: incompatible types: (\w+) cannot be converted to ResourceLocation$",log[i])
  if m and m.group(3) in IDS and i+2<len(log):
    col=log[i+2].index("^")
    fixes[m.group(1)].add((int(m.group(2)),col))
  i+=1
def back(line,col):
  j=col
  while j>0:
    c=line[j-1]
    if c==')':
      d=0
      while j>0:
        j-=1
        if line[j]==')':d+=1
        elif line[j]=='(':
          d-=1
          if d==0:break
    elif c.isalnum() or c in '_.': j-=1
    else: break
  return j
def fwd(line,col):
  j=col
  if line[j]=='.':
    j+=1
    while j<len(line) and (line[j].isalnum() or line[j]=='_'): j+=1
  if j<len(line) and line[j]=='(':
    d=0
    while j<len(line):
      if line[j]=='(':d+=1
      elif line[j]==')':
        d-=1
        if d==0: j+=1;break
      j+=1
  elif line[col].isalnum() or line[col]=='_':
    while j<len(line) and (line[j].isalnum() or line[j]=='_'): j+=1
    while j<len(line) and line[j]=='.' and j+1<len(line) and (line[j+1].isalpha()):
      k=j+1
      while k<len(line) and (line[k].isalnum() or line[k]=='_'): k+=1
      j=k
      if j<len(line) and line[j]=='(':
        d=0
        while j<len(line):
          if line[j]=='(':d+=1
          elif line[j]==')':
            d-=1
            if d==0: j+=1;break
          j+=1
  return j
n=0
for f,locs in fixes.items():
  lines=open(f).read().split("\n")
  for ln,col in sorted(locs,reverse=True):
    line=lines[ln-1]
    end=fwd(line,col)
    lines[ln-1]=line[:end]+".location()"+line[end:]
    n+=1
  open(f,"w").write("\n".join(lines))
print("fixed",n)
