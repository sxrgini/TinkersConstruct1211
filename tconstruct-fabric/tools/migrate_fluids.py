import re,pathlib
expr=r"((?:\w+(?:\([^()]*\))?\.)*\w+(?:\([^()]*\))?)"
for f in pathlib.Path('src/main/java').rglob('*.java'):
  s=o=f.read_text()
  s=re.sub(expr+r"\.getFluidType\(\)", r"FluidTypes.of(\1)", s)
  s=s.replace(".isFluidEqual(",".isSameFluidSameComponents(")
  s=re.sub(r"getTemperature\((?:fluid|stack|\w+)\)","getTemperature()",s)
  if s!=o:
    if "FluidTypes.of(" in s and "import slimeknights.mantle.platform.fluid.FluidTypes;" not in s:
      s=re.sub(r"(package [^\n]*\n\n)", r"\1import slimeknights.mantle.platform.fluid.FluidTypes;\n", s, count=1)
    f.write_text(s)
