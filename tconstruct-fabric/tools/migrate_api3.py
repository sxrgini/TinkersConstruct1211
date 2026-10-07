import re,glob
def addimp(s,imp):
  if "import %s;"%imp in s: return s
  return re.sub(r"(package [^\n]*\n\n)",r"\1import %s;\n"%imp,s,count=1)
for f in glob.glob("src/main/java/**/*.java",recursive=True):
  s=o=open(f).read()
  s=re.sub(r"\b(Operation)\.ADDITION\b",r"\1.ADD_VALUE",s)
  s=re.sub(r"\b(Operation)\.MULTIPLY_BASE\b",r"\1.ADD_MULTIPLIED_BASE",s)
  s=re.sub(r"\b(Operation)\.MULTIPLY_TOTAL\b",r"\1.ADD_MULTIPLIED_TOTAL",s)
  s=s.replace(".saturationMod(",".saturationModifier(").replace(".alwaysEat()",".alwaysEdible()")
  s=re.sub(r"(\b[\w.]+(?:\(\))?)\.readItem\(\)",r"ItemStack.OPTIONAL_STREAM_CODEC.decode(\1)",s)
  s=re.sub(r"(\b[\w.]+)\.writeItem\(([^;]*?)\);",r"ItemStack.OPTIONAL_STREAM_CODEC.encode(\1, \2);",s)
  s=re.sub(r"(\b[\w.]+)\.readFluidStack\(\)",r"FluidStack.OPTIONAL_STREAM_CODEC.decode(\1)",s)
  s=re.sub(r"(\b[\w.]+)\.writeFluidStack\(([^;]*?)\);",r"FluidStack.OPTIONAL_STREAM_CODEC.encode(\1, \2);",s)
  s=re.sub(r"new FluidStack\((\w+(?:\.\w+\(\))*), (\w+(?:\.\w+\(\))*)\)",lambda m: m.group(0),s)
  if s!=o:
    if "ItemStack.OPTIONAL_STREAM_CODEC" in s: s=addimp(s,"net.minecraft.world.item.ItemStack")
    if "FluidStack.OPTIONAL_STREAM_CODEC" in s: s=addimp(s,"slimeknights.mantle.platform.fluid.FluidStack")
    open(f,"w").write(s)
