#!/usr/bin/env python3
"""
Rewrites Forge style registration types to Mantle 1.21 / Fabric shim types:
  RegistryObject<X>   -> DeferredHolder<R, X>   (R = registry base type resolved from X's class hierarchy)
  ItemObject<X>       -> ItemObject<R, X>        (R = Block or Item)
  SynchronizedDeferredRegister -> DeferredRegister
  <register>.register(bus)     -> <register>.register()
Usage: migrate_registries.py <java source dir> <vanilla-classes.txt>
Idempotent. Types that cannot be resolved are left untouched (they show up as compile errors).
"""
import os, re, sys

root, vanilla_file = sys.argv[1], sys.argv[2]
extra_roots = sys.argv[3:]  # additional source roots used only to resolve class hierarchies (e.g. Mantle)

# vanilla simple class name -> package (block/item/material/effect/attributes)
vanilla = {}
for line in open(vanilla_file):
    n, pkg = line.split()
    vanilla.setdefault(n, pkg)

# registry base classes: simple name -> generic form used as the registry type
BASES = {
  'Block': 'Block', 'Item': 'Item', 'Fluid': 'Fluid', 'MobEffect': 'MobEffect', 'Attribute': 'Attribute', 'CreativeModeTab': 'CreativeModeTab',
  'SoundEvent': 'SoundEvent', 'Potion': 'Potion', 'EntityType': 'EntityType<?>', 'BlockEntityType': 'BlockEntityType<?>', 'MenuType': 'MenuType<?>',
  'ParticleType': 'ParticleType<?>', 'RecipeSerializer': 'RecipeSerializer<?>', 'RecipeType': 'RecipeType<?>',
  'LootItemConditionType': 'LootItemConditionType', 'LootItemFunctionType': 'LootItemFunctionType<?>', 'LootPoolEntryType': 'LootPoolEntryType',
  'TypeAwareRecipeSerializer': 'RecipeSerializer<?>', 'GlassBlock': 'Block', 'FluidType': 'FluidType', 'EntityDataSerializer': 'EntityDataSerializer<?>', 'Feature': 'Feature<?>', 'ArgumentTypeInfo': 'ArgumentTypeInfo<?,?>',
}
# class name -> superclass simple name, from TC sources
supers = {}
decl = re.compile(r'\b(?:class|interface|enum|record)\s+(\w+)(?:<[^{]*?>)?\s*(?:\([^)]*\))?\s*(?:extends\s+(\w+))?', re.S)
for d, _, fs in [x for r in [root] + extra_roots for x in os.walk(r)]:
    for f in fs:
        if f.endswith('.java'):
            s = open(os.path.join(d, f), encoding='utf8').read()
            name = f[:-5]
            m = re.search(r'\b(?:class|enum)\s+' + re.escape(name) + r'\b(?:<[^{]*?>)?\s+extends\s+(\w+)', s)
            if m:
                supers[name] = m.group(1)

def resolve(simple):
    seen = set()
    while simple and simple not in seen:
        seen.add(simple)
        if simple in BASES:
            return BASES[simple]
        if simple in supers:
            simple = supers[simple]
            continue
        pkg = vanilla.get(simple)
        if pkg == 'block': return 'Block'
        if pkg == 'item': return 'Item'
        if pkg == 'material': return 'Fluid'
        if pkg == 'effect': return 'MobEffect'
        return None
    return None

def split_generic(text, start):
    """text[start] == '<'; return (inner, end index after matching '>')"""
    depth = 0
    for i in range(start, len(text)):
        if text[i] == '<': depth += 1
        elif text[i] == '>':
            depth -= 1
            if depth == 0:
                return text[start + 1:i], i + 1
    return None, start

def outer_name(t):
    t = t.strip()
    t = re.sub(r'^\?\s+extends\s+', '', t)
    m = re.match(r'(\w+)', t)
    return m.group(1) if m else None

def rewrite(text, keyword, make):
    out, i, changed = [], 0, False
    pat = re.compile(r'\b' + keyword + r'<')
    while True:
        m = pat.search(text, i)
        if not m:
            out.append(text[i:]); break
        lt = m.end() - 1
        inner, end = split_generic(text, lt)
        if inner is None:
            out.append(text[i:m.end()]); i = m.end(); continue
        new = make(inner)
        out.append(text[i:m.start()])
        if new is None:
            out.append(text[m.start():end])
        else:
            out.append(new); changed = True
        i = end
    return ''.join(out), changed

def make_registry_object(inner):
    if ',' in re.sub(r'<[^<>]*>', '', inner) and not inner.strip().startswith('?'):
        pass
    name = outer_name(inner)
    r = resolve(name)
    if r is None:
        return None
    return 'DeferredHolder<' + r + ', ' + inner + '>'

def make_item_object(inner):
    # already two-parameter?
    stripped = re.sub(r'<[^<>]*>', '', inner)
    if ',' in stripped:
        return None
    name = outer_name(inner)
    r = resolve(name)
    if r not in ('Block', 'Item'):
        return None
    return 'ItemObject<' + r + ', ' + inner + '>'

def add_import(text, imp):
    if ('import ' + imp + ';') in text:
        return text
    return re.sub(r'^(package .*;\n)', r'\1\nimport ' + imp + ';', text, count=1, flags=re.M)

n = 0
for d, _, fs in os.walk(root):
    for f in fs:
        if not f.endswith('.java'): continue
        p = os.path.join(d, f)
        s = open(p, encoding='utf8').read()
        t = s
        t, c1 = rewrite(t, 'RegistryObject', make_registry_object)
        t, c2 = rewrite(t, 'ItemObject', make_item_object)
        if c1:
            t = add_import(t, 'slimeknights.mantle.platform.registry.DeferredHolder')
            if 'RegistryObject' not in re.sub(r'import [^;]*;', '', t):
                t = t.replace('import net.minecraftforge.registries.RegistryObject;\n', '')
        t = t.replace('SynchronizedDeferredRegister', 'DeferredRegister')
        if 'DeferredRegister' in t:
            t = t.replace('import slimeknights.mantle.registration.deferred.DeferredRegister;\n', 'import slimeknights.mantle.platform.registry.DeferredRegister;\n')
            t = t.replace('import net.minecraftforge.registries.DeferredRegister;\n', 'import slimeknights.mantle.platform.registry.DeferredRegister;\n')
        t = re.sub(r'\.register\((?:bus|modEventBus|eventBus)\)', '.register()', t)
        if t != s:
            open(p, 'w', encoding='utf8').write(t); n += 1
print('rewrote', n, 'files')
