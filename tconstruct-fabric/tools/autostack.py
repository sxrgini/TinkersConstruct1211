"""Rewrites stack.getTag()/getOrCreateTag()/hasTag()/setTag(x) on ItemStack receivers reported by javac into StackNbt calls"""
import re,sys,collections
log=open(sys.argv[1]).read().split("\n")
M={"getTag":1,"getOrCreateTag":1,"hasTag":1,"setTag":1}
fixes=collections.defaultdict(set)
for i,l in enumerate(log):
  m=re.match(r"(/\S+\.java):(\d+): error: cannot find symbol",l)
  if m and i+4<len(log):
    sym=" ".join(log[i+1:i+6])
    mm=re.search(r"symbol:\s+method (getTag|getOrCreateTag|hasTag|setTag)\(",sym)
    if mm and re.search(r"location: (variable \w+ of type|class) ItemStack",sym):
      col=log[i+2].index("^")
      fixes[m.group(1)].add((int(m.group(2)),col,mm.group(1)))
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
n=0
for f,locs in fixes.items():
  lines=open(f).read().split("\n")
  for ln,col,name in sorted(locs,reverse=True):
    line=lines[ln-1]
    if line[col]!='.': continue
    st=back(line,col)
    recv=line[st:col]
    k=col+1+len(name)
    if line[k]!='(': continue
    # find matching paren
    d=0;e=k
    while e<len(line):
      if line[e]=='(':d+=1
      elif line[e]==')':
        d-=1
        if d==0:break
      e+=1
    arg=line[k+1:e]
    args=recv+(", "+arg if arg else "")
    lines[ln-1]=line[:st]+"StackNbt.%s(%s)"%(name,args)+line[e+1:]
    n+=1
  s="\n".join(lines)
  if "import slimeknights.tconstruct.library.utils.StackNbt;" not in s and not re.search(r"^package slimeknights\.tconstruct\.library\.utils;",s,re.M):
    s=re.sub(r"(package [^\n]*\n\n)",r"\1import slimeknights.tconstruct.library.utils.StackNbt;\n",s,count=1)
  open(f,"w").write(s)
print("stack fixes",n)
