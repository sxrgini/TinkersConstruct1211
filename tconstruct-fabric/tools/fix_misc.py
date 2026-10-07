import re,sys,collections
log=open('/tmp/claude-0/tc.log').read().split("\n")
def addimp(s,imp):
  if "import %s;"%imp in s: return s
  return re.sub(r"(package [^\n]*\n\n)",lambda m:m.group(1)+"import %s;\n"%imp,s,count=1)
fix=collections.defaultdict(set)
for i,l in enumerate(log):
  m=re.match(r"(/\S+\.java):(\d+): error: cannot find symbol",l)
  if not m: continue
  blk=" ".join(log[i+1:i+7])
  if "method broadcastBreakEvent(InteractionHand)" in blk: fix[m.group(1)].add((int(m.group(2)),"bbe_hand"))
  elif "method broadcastBreakEvent(EquipmentSlot)" in blk: fix[m.group(1)].add((int(m.group(2)),"bbe_slot"))
  elif re.search(r"method use\(\w+,\w+,InteractionHand,BlockHitResult\)",blk): fix[m.group(1)].add((int(m.group(2)),"use"))
n=0
for f,items in fix.items():
  lines=open(f).read().split("\n")
  for ln,k in items:
    l=lines[ln-1]
    if k=="bbe_hand":
      l2=re.sub(r"(\w+)\.broadcastBreakEvent\(([^;]+)\);",r"\1.onEquippedItemBroken(\1.getItemInHand(\2).getItem(), LivingEntity.getSlotForHand(\2));",l)
    elif k=="bbe_slot":
      l2=re.sub(r"(\w+)\.broadcastBreakEvent\(([^;]+)\);",r"\1.onEquippedItemBroken(\1.getItemBySlot(\2).getItem(), \2);",l)
    else:
      l2=re.sub(r"(\w+)\.use\(([^;]+)\);",r"PlatformHooks.useBlock(\1, \2);",l)
    if l2!=l: lines[ln-1]=l2;n+=1
  s="\n".join(lines)
  if "PlatformHooks.useBlock" in s: s=addimp(s,"slimeknights.mantle.platform.PlatformHooks")
  if "LivingEntity.getSlotForHand" in s: s=addimp(s,"net.minecraft.world.entity.LivingEntity")
  open(f,"w").write(s)
print("misc",n)
