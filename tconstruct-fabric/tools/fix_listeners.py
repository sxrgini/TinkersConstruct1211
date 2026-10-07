import re,glob,os
files=glob.glob("src/main/java/**/*.java",recursive=True)
by_name={os.path.basename(f)[:-5]:f for f in files}
def param_type(src,method):
  m=re.search(r"\b%s\(\s*(?:final )?([\w.<>?]+) \w+\s*\)"%re.escape(method),src)
  return m.group(1) if m else None
for f in files:
  s=o=open(f).read()
  def repl(m):
    bus,prio,ref=m.group(1),m.group(2) or "",m.group(3)
    mm=re.match(r"(\w+)::(\w+)$",ref)
    if not mm: return m.group(0)
    owner,meth=mm.groups()
    if owner in("this",) or owner==os.path.basename(f)[:-5] or owner=="INSTANCE":
      src=s
    else:
      src=open(by_name[owner]).read() if owner in by_name else ""
    if owner=="INSTANCE":
      src=s
    t=param_type(src,meth)
    if not t: return m.group(0)
    t=t.split("<")[0]
    return "EventBus.%s.addListener(%s%s.class, %s)"%(bus,prio,t,ref)
  s=re.sub(r"EventBus\.(BUS|MOD_BUS)\.addListener\((EventPriority\.\w+, )?((?:\w+)::\w+)\)",repl,s)
  if s!=o: open(f,"w").write(s); print(f)
