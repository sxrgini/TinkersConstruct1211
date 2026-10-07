import re,glob,sys
T=r"[\w<>?,.\[\] ]+?"
def addimp(s,imp):
  if "import %s;"%imp in s: return s
  return re.sub(r"(package [^\n]*\n\n)",r"\1import %s;\n"%imp,s,count=1)
n=0
for f in glob.glob("src/main/java/**/*.java",recursive=True):
  s=o=open(f).read()
  if not re.search(r"Loadable|RecordField|LoadableField|Streamable",s): continue
  s=re.sub(r"(@Override\s+public JsonElement serialize\(%s \w+)\)"%T,r"\1, TypedMap context)",s)
  s=re.sub(r"(@Override\s+public )void serialize\((%s \w+), JsonObject (\w+)\)"%T,r"\1void serializeInto(\2, JsonObject \3, TypedMap context)",s)
  s=re.sub(r"(@Override\s+public void serializeInto\(%s \w+, JsonObject \w+)\)"%T,r"\1, TypedMap context)",s)
  s=re.sub(r"(@Override\s+public void encode\(RegistryFriendlyByteBuf \w+, %s \w+)\)"%T,r"\1, TypedMap context)",s)
  s=re.sub(r"(@Override\s+public String getString\(%s \w+)\)"%T,r"\1, TypedMap context)",s)
  if s!=o:
    s=addimp(s,"slimeknights.mantle.util.typed.TypedMap")
    open(f,"w").write(s);n+=1
print(n)
