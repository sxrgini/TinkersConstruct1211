#!/usr/bin/env python3
"""
Mechanical Forge -> Fabric/Mantle-shim import migration for the TConstruct port.
Only rewrites things that have a 1:1 equivalent; everything else is left for manual porting.
Usage: migrate_imports.py <java source dir>
Idempotent: safe to run repeatedly.
"""
import os, re, sys

P = 'slimeknights.mantle.platform.'
# fully qualified Forge class -> (new fully qualified class, new simple name or None to keep)
CLASS_MAP = {
  'net.minecraftforge.fluids.FluidStack': P+'fluid.FluidStack',
  'net.minecraftforge.fluids.FluidType': P+'fluid.FluidType',
  'net.minecraftforge.fluids.ForgeFlowingFluid': (P+'fluid.BaseFlowingFluid', 'BaseFlowingFluid'),
  'net.minecraftforge.fluids.capability.IFluidHandler': P+'fluid.IFluidHandler',
  'net.minecraftforge.fluids.capability.IFluidHandlerItem': P+'fluid.IFluidHandlerItem',
  'net.minecraftforge.common.crafting.conditions.ICondition': P+'condition.ICondition',
  'net.minecraftforge.client.model.data.ModelData': P+'client.model.ModelData',
  'net.minecraftforge.client.model.data.ModelProperty': P+'client.model.ModelProperty',
  'net.minecraftforge.client.model.geometry.IGeometryBakingContext': P+'client.model.IGeometryBakingContext',
  'net.minecraftforge.client.model.geometry.IGeometryLoader': P+'client.model.IGeometryLoader',
  'net.minecraftforge.client.model.geometry.IUnbakedGeometry': P+'client.model.IUnbakedGeometry',
  'net.minecraftforge.client.model.geometry.UnbakedGeometryHelper': P+'client.model.UnbakedGeometryHelper',
  'net.minecraftforge.client.model.IQuadTransformer': P+'client.model.IQuadTransformer',
  'net.minecraftforge.client.model.QuadTransformers': P+'client.model.QuadTransformers',
  'net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions': P+'client.IClientFluidTypeExtensions',
  'net.minecraftforge.common.data.ExistingFileHelper': P+'data.ExistingFileHelper',
  'net.minecraftforge.items.IItemHandler': P+'item.IItemHandler',
  'net.minecraftforge.items.IItemHandlerModifiable': P+'item.IItemHandlerModifiable',
  'net.minecraftforge.items.wrapper.InvWrapper': P+'item.InvWrapper',
  'net.minecraftforge.items.SlotItemHandler': P+'item.SlotItemHandler',
  'net.minecraftforge.common.ToolAction': (P+'item.ItemAbility', 'ItemAbility'),
  'net.minecraftforge.common.ToolActions': (P+'item.ItemAbilities', 'ItemAbilities'),
  'net.minecraftforge.common.crafting.conditions.TrueCondition': P+'condition.TrueCondition',
  'net.minecraftforge.common.crafting.conditions.FalseCondition': P+'condition.FalseCondition',
  'net.minecraftforge.common.crafting.conditions.NotCondition': P+'condition.NotCondition',
  'net.minecraftforge.api.distmarker.Dist': 'net.fabricmc.api.EnvType',
  'net.minecraftforge.fml.ModList': 'net.fabricmc.loader.api.FabricLoader',
}
# nested imports: Forge outer.Inner -> new import (drop if None)
NESTED = {
  'net.minecraftforge.fluids.capability.IFluidHandler.FluidAction': P+'fluid.IFluidHandler.FluidAction',
  'net.minecraftforge.common.crafting.conditions.ICondition.IContext': P+'condition.ICondition.IContext',
}
# simple-name renames applied in code when the class was renamed
RENAMES = {'ForgeFlowingFluid': 'BaseFlowingFluid', 'ToolActions': 'ItemAbilities', 'ToolAction': 'ItemAbility'}

imp = re.compile(r'^import (static )?(net\.minecraftforge\.[\w.]+);\n', re.M)

def migrate(text):
    changed = False
    renames = {}
    def repl(m):
        nonlocal changed
        static, name = m.group(1) or '', m.group(2)
        if name in NESTED:
            changed = True
            return f'import {static}{NESTED[name]};\n'
        # static member imports: net.minecraftforge.X.Class.MEMBER
        if static:
            for k, v in CLASS_MAP.items():
                if name.startswith(k + '.'):
                    nv = v[0] if isinstance(v, tuple) else v
                    changed = True
                    return f'import static {nv}{name[len(k):]};\n'
            return m.group(0)
        if name in CLASS_MAP:
            v = CLASS_MAP[name]
            changed = True
            if isinstance(v, tuple):
                renames[name.rsplit('.', 1)[1]] = v[1]
                return f'import {v[0]};\n'
            return f'import {v};\n'
        return m.group(0)
    new = imp.sub(repl, text)
    for old, nw in renames.items():
        new = re.sub(r'\b' + old + r'\b', nw, new)
    return new, changed

def main(root):
    n = 0
    for d, _, fs in os.walk(root):
        for f in fs:
            if f.endswith('.java'):
                p = os.path.join(d, f)
                s = open(p, encoding='utf8').read()
                t, ch = migrate(s)
                if ch and t != s:
                    open(p, 'w', encoding='utf8').write(t); n += 1
    print('rewrote', n, 'files')

if __name__ == '__main__':
    main(sys.argv[1])
