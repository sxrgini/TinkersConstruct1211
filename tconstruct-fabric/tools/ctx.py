import re,sys
log=open('/tmp/claude-0/tc.log').read().split("\n")
pat=sys.argv[1]; before=int(sys.argv[2]) if len(sys.argv)>2 else 1; after=int(sys.argv[3]) if len(sys.argv)>3 else 1
seen=set()
for i,l in enumerate(log):
  m=re.match(r"(/\S+\.java):(\d+): error: (.*)",l)
  if not m or not re.search(pat,m.group(1)): continue
  k=(m.group(1),m.group(2),m.group(3))
  if k in seen: continue
  seen.add(k)
  n=int(m.group(2))
  src=open(m.group(1)).read().split("\n")
  sym=" ".join(x.strip() for x in log[i+1:i+7] if "symbol:" in x or "required:" in x)[:100]
  print("--- %s:%d %s %s"%(m.group(1).split("/tconstruct/")[-1],n,m.group(3)[:100],sym))
  for j in range(max(1,n-before),min(len(src),n+after)+1):
    print("%s%d| %s"%(">" if j==n else " ",j,src[j-1][:170]))
