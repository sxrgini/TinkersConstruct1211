import re,sys
log=open(sys.argv[1]).read()
import glob
files=glob.glob("src/main/java/**/*.java",recursive=True)
n=0
for f in files:
  s=o=open(f).read()
  s=re.sub(r"\b(saveSynced|saveAdditional)\(CompoundTag (\w+)\)",r"\1(CompoundTag \2, HolderLookup.Provider registries)",s)
  s=re.sub(r"\bsuper\.(saveSynced|saveAdditional)\((\w+)\)",r"super.\1(\2, registries)",s)
  s=re.sub(r"\bpublic void load\(CompoundTag (\w+)\)",r"public void loadAdditional(CompoundTag \1, HolderLookup.Provider registries)",s)
  s=re.sub(r"\bsuper\.load\((\w+)\)",r"super.loadAdditional(\1, registries)",s)
  if s!=o:
    if "import net.minecraft.core.HolderLookup;" not in s:
      s=re.sub(r"(package [^\n]*\n\n)",r"\1import net.minecraft.core.HolderLookup;\n",s,count=1)
    open(f,"w").write(s); n+=1
print(n)
