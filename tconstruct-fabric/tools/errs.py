import re,sys,collections
log=open('/tmp/claude-0/tc.log').read().split("\n")
errs=[];seen=set()
for i,l in enumerate(log):
  m=re.match(r"(/\S+\.java):(\d+): error: (.*)",l)
  if not m: continue
  k=(m.group(1),m.group(2),m.group(3))
  if k in seen: continue
  seen.add(k)
  src=log[i+1].strip()[:110] if i+1<len(log) else ""
  sym=""
  for x in log[i+2:i+6]:
    if "symbol:" in x or "required:" in x: sym=" ["+x.strip()[:70]+"]";break
  errs.append((m.group(1).split("/tconstruct/")[-1],int(m.group(2)),m.group(3)[:90]+sym,src))
if len(sys.argv)>1:
  pat=sys.argv[1]
  for e in errs:
    if re.search(pat,e[0]): print("%s:%d %s | %s"%e)
else:
  c=collections.Counter(e[0] for e in errs)
  print(len(errs),"unique")
  for k,v in c.most_common(60): print(v,k)
