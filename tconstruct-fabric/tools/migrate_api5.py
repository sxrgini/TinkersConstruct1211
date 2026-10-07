import re,glob
def addimp(s,imp):
  if "import %s;"%imp in s: return s
  return re.sub(r"(package [^\n]*\n\n)",r"\1import %s;\n"%imp,s,count=1)
def balanced(s,i):
  d=0;j=i
  while j<len(s):
    if s[j]=="(":d+=1
    elif s[j]==")":
      d-=1
      if d==0:return j
    j+=1
  return -1
for f in glob.glob("src/main/java/**/*.java",recursive=True):
  s=o=open(f).read()
  s=s.replace("ComponentContents.EMPTY","PlainTextContents.EMPTY")
  s=re.sub(r"\.getSlotIndex\(\)",".getContainerSlot()",s)
  s=re.sub(r"\bthis\.textField\.tick\(\);\n","",s)
  # remove .setBackground(...) calls
  while True:
    m=re.search(r"\.setBackground\(",s)
    if not m: break
    e=balanced(s,m.end()-1)
    s=s[:m.start()]+s[e+1:]
  s=re.sub(r"\n\s*setBackground\([^;]*\);","",s)
  s=re.sub(r"public boolean isHovering\(Slot (\w+), double (\w+), double (\w+)\)",r"protected boolean isHovering(Slot \1, double \2, double \3)",s)
  if "scrollX" in s:
    s=re.sub(r"super\.(mouseScrolled|handleMouseScrolled)\((\w+), (\w+), (\w+)\)",r"super.\1(\2, \3, scrollX, \4)",s)
  s=re.sub(r"(\w+)\.getMinecraft\(\)",lambda m: "Minecraft.getInstance()" if m.group(1) in ("parent","this.parent") or m.group(0).startswith("this.parent") else m.group(0),s)
  s=s.replace("this.parent.getMinecraft()","Minecraft.getInstance()")
  if s!=o:
    if "PlainTextContents" in s: s=addimp(s,"net.minecraft.network.chat.contents.PlainTextContents")
    if "Minecraft.getInstance()" in s: s=addimp(s,"net.minecraft.client.Minecraft")
    open(f,"w").write(s)
