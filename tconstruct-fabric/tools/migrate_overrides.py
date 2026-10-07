"""Drops @Override on Forge-only hooks (recording them as porting gaps) and ports appendHoverText"""
import re,sys,collections
log=open(sys.argv[1]).read().split("\n")
DROP={"getMaxStackSize","isNotReplaceableByPickAction","getEquipmentSlot","isBookEnchantable","canApplyAtEnchantingTable","getEnchantmentLevel","getAllEnchantments","verifyTagAfterLoad","getRarity","hasCustomEntity","createEntity","getDefaultTooltipHideFlags","isRepairable","canBeDepleted","getMaxDamage","getDamage","setDamage","damageItem","onLeftClickEntity","getAttributeModifiers","canDisableShield","onBlockStartBreak","onItemUseFirst","canContinueUsing","onStopUsing","canPerformAction","shouldCauseBlockBreakReset","shouldCauseReequipAnimation","isInfinite","isEnderMask","getArmorTexture","getCreatorModId","getCraftingRemainingItem","hasCraftingRemainingItem","makesPiglinsNeutral","canWalkOnPowderedSnow","canElytraFly","elytraFlightTick","getArmPose","onSheared","getToolModifiedState","getBeaconColorMultiplier","isSlimeBlock","canStickTo","isStickyBlock","getPickedResult","getUpdateTag","handleUpdateTag","getRenderBoundingBox","onMinecartPass","brokenByPlayer","getBlockPathType","getModelData","onLoad","isValidBonemealTarget","getCurativeItems"}
fixes=collections.defaultdict(set)
for i,l in enumerate(log):
  m=re.match(r"(/\S+\.java):(\d+): error: method does not override",l)
  if m: fixes[m.group(1)].add(int(m.group(2)))
gaps=[]
for f,lns in fixes.items():
  lines=open(f).read().split("\n")
  for n in sorted(lns,reverse=True):
    if not lines[n-1].strip().startswith("@Override"): continue
    j=n
    while j<len(lines) and not re.search(r"\w+\s*\(",lines[j]): j+=1
    mm=re.search(r"(\w+)\s*\(",lines[j])
    name=mm.group(1)
    if name in DROP:
      gaps.append((f.split("/tconstruct/")[1],name))
      del lines[n-1]
    elif name=="appendHoverText":
      # port to TooltipContext
      hdr=lines[j]
      h=re.sub(r"(@Nullable )?(Level|BlockGetter) (\w+)",r"Item.TooltipContext context",hdr,count=1)
      lines[j]=h
      var=re.search(r"(?:@Nullable )?(Level|BlockGetter) (\w+)",hdr)
      if var and var.group(1)=="Level":
        k=j
        while "{" not in lines[k]: k+=1
        lines.insert(k+1,"    Level %s = context.level();"%var.group(2))
  s="\n".join(lines)
  if "Item.TooltipContext" in s and "import net.minecraft.world.item.Item;" not in s:
    s=re.sub(r"(package [^\n]*\n\n)",r"\1import net.minecraft.world.item.Item;\n",s,count=1)
  open(f,"w").write(s)
open("docs/porting-gaps-hooks.txt","a").write("\n".join("%s\t%s"%g for g in gaps)+"\n")
print(len(gaps))
