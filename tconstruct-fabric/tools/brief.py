import re,sys
pat=sys.argv[1] if len(sys.argv)>1 else "."
lines=open("/tmp/claude-0/tc.log").read().split("\n")
seen=set()
for i,l in enumerate(lines):
    m=re.match(r"^/home/\S+/tconstruct/(\S+?):(\d+): error: (.*)",l)
    if m and re.search(pat,m.group(1)):
        k=(m.group(1),m.group(2),m.group(3))
        if k in seen: continue
        seen.add(k)
        print(f"{m.group(1)}:{m.group(2)}: {m.group(3)[:110]} || {lines[i+1].strip()[:150]}")
