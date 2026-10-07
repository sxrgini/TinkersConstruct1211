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
  'net.minecraftforge.common.Tags': P+'tags.Tags',
  'net.minecraftforge.api.distmarker.Dist': 'net.fabricmc.api.EnvType',
  'net.minecraftforge.fml.ModList': 'net.fabricmc.loader.api.FabricLoader',
}
E = P + 'event.'
# classes that exist in Mantle's event system: Forge package -> Mantle package
for living in ('LivingEvent', 'LivingAttackEvent', 'LivingHurtEvent', 'LivingDamageEvent', 'LivingDeathEvent', 'LivingKnockBackEvent', 'LivingFallEvent',
               'LivingDropsEvent', 'LivingExperienceDropEvent', 'LivingEquipmentChangeEvent', 'ShieldBlockEvent', 'LootingLevelEvent', 'LivingGetProjectileEvent'):
    CLASS_MAP['net.minecraftforge.event.entity.living.' + living] = E + 'living.' + living
for player in ('PlayerEvent', 'CriticalHitEvent', 'AttackEntityEvent', 'PlayerInteractEvent'):
    CLASS_MAP['net.minecraftforge.event.entity.player.' + player] = E + 'player.' + player
for ent in ('EntityTeleportEvent', 'ProjectileImpactEvent'):
    CLASS_MAP['net.minecraftforge.event.entity.' + ent] = E + 'entity.' + ent
CLASS_MAP.update({
  'net.minecraftforge.eventbus.api.SubscribeEvent': E + 'SubscribeEvent',
  'net.minecraftforge.eventbus.api.EventPriority': E + 'EventPriority',
  'net.minecraftforge.eventbus.api.Event': E + 'Event',
  'net.minecraftforge.eventbus.api.Cancelable': E + 'Event.Cancelable',
  'net.minecraftforge.common.MinecraftForge': E + 'EventBus',
})
# nested imports: Forge outer.Inner -> new import (drop if None)
NESTED = {
  'net.minecraftforge.fluids.capability.IFluidHandler.FluidAction': P+'fluid.IFluidHandler.FluidAction',
  'net.minecraftforge.eventbus.api.Event.Result': P+'event.Event.Result',
  'net.minecraftforge.common.crafting.conditions.ICondition.IContext': P+'condition.ICondition.IContext',
}
# simple-name renames applied in code when the class was renamed
RENAMES = {'ForgeFlowingFluid': 'BaseFlowingFluid', 'ToolActions': 'ItemAbilities', 'ToolAction': 'ItemAbility'}

imp = re.compile(r'^import (static )?(net\.minecraftforge\.[\w.]+);\n', re.M)

def nested_event(name):
    for k, v in list(CLASS_MAP.items()):
        if k.startswith('net.minecraftforge.event.') and name.startswith(k + '.'):
            return (v if isinstance(v, str) else v[0]) + name[len(k):]
    return None

def migrate(text):
    changed = False
    renames = {}
    def repl(m):
        nonlocal changed
        static, name = m.group(1) or '', m.group(2)
        ne = nested_event(name) if name not in CLASS_MAP else None
        if ne:
            changed = True
            return f'import {static}{ne};\n'
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
    if 'MinecraftForge.EVENT_BUS' in new:
        new = new.replace('MinecraftForge.EVENT_BUS', 'EventBus.BUS')
        changed = True
    return new, changed

REGISTRY_NAMES = {
  'ITEMS': 'ITEM', 'BLOCKS': 'BLOCK', 'FLUIDS': 'FLUID', 'ENTITY_TYPES': 'ENTITY_TYPE', 'BLOCK_ENTITY_TYPES': 'BLOCK_ENTITY_TYPE',
  'MOB_EFFECTS': 'MOB_EFFECT', 'POTIONS': 'POTION', 'RECIPE_TYPES': 'RECIPE_TYPE', 'RECIPE_SERIALIZERS': 'RECIPE_SERIALIZER',
  'MENU_TYPES': 'MENU', 'SOUND_EVENTS': 'SOUND_EVENT', 'ATTRIBUTES': 'ATTRIBUTE', 'PARTICLE_TYPES': 'PARTICLE_TYPE', 'FEATURES': 'FEATURE',
}
def migrate_registries(text):
    if 'ForgeRegistries' not in text:
        return text, False
    new = text
    for old, nw in REGISTRY_NAMES.items():
        new = re.sub(r'\bForgeRegistries\.' + old + r'\b', 'BuiltInRegistries.' + nw, new)
        new = re.sub(r'\bForgeRegistries\.Keys\.' + old + r'\b', 'Registries.' + nw, new)
    if new == text:
        return text, False
    if 'BuiltInRegistries.' in new and 'import net.minecraft.core.registries.BuiltInRegistries;' not in new:
        new = re.sub(r'^(package .*;\n)', r'\1\nimport net.minecraft.core.registries.BuiltInRegistries;', new, count=1, flags=re.M)
    if 'Registries.' in re.sub(r'BuiltInRegistries\.', '', new) and 'import net.minecraft.core.registries.Registries;' not in new:
        new = re.sub(r'^(package .*;\n)', r'\1\nimport net.minecraft.core.registries.Registries;', new, count=1, flags=re.M)
    if 'ForgeRegistries' not in new:
        new = new.replace('import net.minecraftforge.registries.ForgeRegistries;\n', '')
    return new, True

def main(root):
    n = 0
    for d, _, fs in os.walk(root):
        for f in fs:
            if f.endswith('.java'):
                p = os.path.join(d, f)
                s = open(p, encoding='utf8').read()
                t, ch = migrate(s)
                t, ch2 = migrate_registries(t)
                ch = ch or ch2
                if ch and t != s:
                    open(p, 'w', encoding='utf8').write(t); n += 1
    print('rewrote', n, 'files')

if __name__ == '__main__':
    main(sys.argv[1])
