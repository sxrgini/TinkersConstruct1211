import re,glob
def body_range(s,start):
  """start: index of method header start; returns (open,close) brace indices of body"""
  i=s.index("{",s.index(")",start))
  d=0;j=i
  while j<len(s):
    c=s[j]
    if c=="{":d+=1
    elif c=="}":
      d-=1
      if d==0:return i,j
    j+=1
  return i,len(s)-1
def addimp(s,imp):
  if "import %s;"%imp in s: return s
  return re.sub(r"(package [^\n]*\n\n)",r"\1import %s;\n"%imp,s,count=1)
def sub_method(s,pat,fn):
  """for each header match, call fn(s,m)->new s (re-searching from scratch until stable)"""
  pos=0
  while True:
    m=re.compile(pat,re.S).search(s,pos)
    if not m:return s
    s2,newpos=fn(s,m)
    s=s2;pos=newpos
n=0
for f in glob.glob("src/main/java/**/*.java",recursive=True):
  s=o=open(f).read()
  # 1 appendHoverText
  def hover(s,m):
    typ,var=m.group(2),m.group(3)
    hdr=m.group(0)
    new=hdr.replace(m.group(1)+typ+" "+var,"Item.TooltipContext context")
    o_,c_=body_range(s,m.start())
    body=s[o_:c_]
    if typ=="Level" and re.search(r"\b%s\b"%var,body):
      ins="\n    Level %s = context.level();"%var
      s=s[:o_+1]+ins+s[o_+1:]
    s=s[:m.start()]+new+s[m.end():]
    return s,m.start()+len(new)
  s=sub_method(s,r"appendHoverText\(ItemStack \w+, ((?:@Nullable |@Nonnull )?)(Level|BlockGetter) (\w+), List<Component> \w+, TooltipFlag \w+\)",hover)
  # 2 use -> useWithoutItem
  def use(s,m):
    st,lv,ps,pl,hd,hit=m.group(1),m.group(2),m.group(3),m.group(4),m.group(5),m.group(6)
    new="useWithoutItem(BlockState %s, Level %s, BlockPos %s, Player %s, BlockHitResult %s)"%(st,lv,ps,pl,hit)
    o_,c_=body_range(s,m.start())
    body=s[o_:c_]
    if re.search(r"\b%s\b"%hd,body):
      s=s[:o_+1]+"\n    InteractionHand %s = InteractionHand.MAIN_HAND;"%hd+s[o_+1:]
    s=s[:m.start()]+new+s[m.end():]
    return s,m.start()+len(new)
  s=sub_method(s,r"(?<=InteractionResult )use\(BlockState (\w+), Level (\w+), BlockPos (\w+), Player (\w+), InteractionHand (\w+), BlockHitResult (\w+)\)",use)
  # 3 getCloneItemStack
  s=re.sub(r"getCloneItemStack\(BlockState (\w+), HitResult \w+, BlockGetter (\w+), BlockPos (\w+), Player \w+\)",r"getCloneItemStack(LevelReader \2, BlockPos \3, BlockState \1)",s)
  s=re.sub(r"getCloneItemStack\(BlockGetter (\w+), BlockPos (\w+), BlockState (\w+)\)",r"getCloneItemStack(LevelReader \1, BlockPos \2, BlockState \3)",s)
  # 4 isPathfindable
  s=re.sub(r"isPathfindable\(BlockState (\w+), BlockGetter \w+, BlockPos \w+, PathComputationType (\w+)\)",r"isPathfindable(BlockState \1, PathComputationType \2)",s)
  # 5 mob effects
  s=re.sub(r"\bisDurationEffectTick\(int (\w+), int (\w+)\)",r"shouldApplyEffectTickThisTick(int \1, int \2)",s)
  def apply(s,m):
    o_,c_=body_range(s,m.start())
    body=s[o_:c_]
    body2=re.sub(r"\breturn;","return true;",body)
    body2=body2.rstrip()+"\n    return true;\n  "
    new=m.group(0).replace("void applyEffectTick","boolean applyEffectTick")
    s=s[:m.start()]+new+body2+s[c_:]
    return s,m.start()+len(new)
  s=sub_method(s,r"(?<=public )void applyEffectTick\((?:@Nonnull )?LivingEntity \w+, int \w+\)",apply) if False else s
  # 6 defineSynchedData
  def dsd(s,m):
    o_,c_=body_range(s,m.start())
    body=s[o_:c_]
    body=re.sub(r"\bthis\.entityData\.define\(","builder.define(",body)
    body=re.sub(r"(?<![\w.])entityData\.define\(","builder.define(",body)
    s=s[:m.start()]+"defineSynchedData(SynchedEntityData.Builder builder)"+s[m.end():o_]+body+s[c_:]
    return s,m.start()+10
  if re.search(r"void defineSynchedData\(\)",s):
    s=sub_method(s,r"defineSynchedData\(\)",dsd) if False else s
  s=re.sub(r"\bprotected void defineSynchedData\(\) \{",r"protected void defineSynchedData(SynchedEntityData.Builder builder) {",s)
  s=re.sub(r"\bthis\.entityData\.define\(","builder.define(",s) if "SynchedEntityData.Builder builder" in s else s
  s=s.replace("super.defineSynchedData();","super.defineSynchedData(builder);")
  # 7 mouseScrolled
  s=re.sub(r"mouseScrolled\(double (\w+), double (\w+), double (\w+)\)",r"mouseScrolled(double \1, double \2, double scrollX, double \3)",s)
  # 10 crafting recipes
  s=re.sub(r"\b(matches|assemble|getRemainingItems)\(CraftingContainer ",r"\1(CraftingInput ",s)
  if s!=o:
    for cls,imp in (("SynchedEntityData.Builder","net.minecraft.network.syncher.SynchedEntityData"),("LevelReader","net.minecraft.world.level.LevelReader"),("CraftingInput","net.minecraft.world.item.crafting.CraftingInput"),("InteractionHand","net.minecraft.world.InteractionHand"),("BlockHitResult","net.minecraft.world.phys.BlockHitResult"),("Item.TooltipContext","net.minecraft.world.item.Item")):
      if cls in s: s=addimp(s,imp)
    open(f,"w").write(s);n+=1
print(n)
