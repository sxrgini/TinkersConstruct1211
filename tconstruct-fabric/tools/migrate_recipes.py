#!/usr/bin/env python3
"""
Migrates TConstruct recipes to the Minecraft 1.21 / Mantle 1.21 recipe model. Idempotent. Usage: migrate_recipes.py <java dir>
  - recipes no longer store their ID: removes ContextKey.ID loader fields, `ResourceLocation id` constructor params/fields, and `id` args to super/this/new calls of the same class
  - recipe containers become RecipeInputs: ISingleStackContainer -> SingleItemInput (getStack() -> getItem()), IEmptyContainer -> NoItemInput, IRecipeContainer -> RecipeInput
  - getResultItem / assemble take HolderLookup.Provider instead of RegistryAccess
"""
import os, re, sys

M = 'slimeknights.mantle.'
CONTAINERS = {
  M + 'recipe.container.ISingleStackContainer': (M + 'recipe.input.SingleItemInput', 'SingleItemInput'),
  M + 'recipe.container.IEmptyContainer': (M + 'recipe.input.NoItemInput', 'NoItemInput'),
  M + 'recipe.container.IRecipeContainer': ('net.minecraft.world.item.crafting.RecipeInput', 'RecipeInput'),
}

def add_import(text, imp):
    if f'import {imp};' in text: return text
    return re.sub(r'^(package .*;\n)', r'\1\nimport ' + imp + ';', text, count=1, flags=re.M)

def strip_id(text, cls):
    """removes the recipe id from class `cls` declared in this file"""
    t = text
    # loader field
    t = re.sub(r'ContextKey\.ID\.requiredField\(\),\s*', '', t)
    # constructor parameter lists: (ResourceLocation id, ...) for constructors of this class
    t = re.sub(r'(\b(?:public|protected|private)?\s*' + re.escape(cls) + r'\()\s*ResourceLocation id\s*,\s*', r'\1', t)
    t = re.sub(r'(\b(?:public|protected|private)?\s*' + re.escape(cls) + r'\()\s*ResourceLocation id\s*\)', r'\1)', t)
    # this(id, ...) / super(id, ...) / new Cls(id, ...)
    t = re.sub(r'\b(this|super)\(\s*id\s*,\s*', r'\1(', t)
    t = re.sub(r'\b(this|super)\(\s*id\s*\)', r'\1()', t)
    t = re.sub(r'\bnew ' + re.escape(cls) + r'\(\s*id\s*,\s*', 'new ' + cls + '(', t)
    t = re.sub(r'\bnew ' + re.escape(cls) + r'\(\s*id\s*\)', 'new ' + cls + '()', t)
    # id field + getter annotation + assignment
    t = re.sub(r'\n\s*@Getter\s*\n(\s*)(?:private|protected)\s+final\s+ResourceLocation id;', r'\n', t)
    t = re.sub(r'\n\s*(?:private|protected)\s+final\s+ResourceLocation id;', '\n', t)
    t = re.sub(r'\n\s*this\.id = id;', '', t)
    return t

def migrate(text, name):
    t = text
    for old, (new, simple) in CONTAINERS.items():
        if f'import {old};' in t:
            t = t.replace(f'import {old};', f'import {new};')
            t = re.sub(r'\b' + old.rsplit('.', 1)[1] + r'\b', simple, t)
    if 'SingleItemInput' in t:
        t = re.sub(r'\.getStack\(\)', '.getItem()', t)
    if 'ContextKey.ID' in t or re.search(r'\(ResourceLocation id[,)]', t):
        if re.search(r'class\s+' + re.escape(name) + r'\b', t):
            t = strip_id(t, name)
    # RegistryAccess -> HolderLookup.Provider in the recipe result methods
    def prov(m):
        return m.group(1) + 'HolderLookup.Provider ' + m.group(2)
    new = re.sub(r'((?:getResultItem|assemble)\([^)]*?)RegistryAccess (\w+)', prov, t)
    if new != t:
        t = add_import(new, 'net.minecraft.core.HolderLookup')
    return t

n = 0
for d, _, fs in os.walk(sys.argv[1]):
    for f in fs:
        if f.endswith('.java'):
            p = os.path.join(d, f); s = open(p, encoding='utf8').read()
            t = migrate(s, f[:-5])
            if t != s: open(p, 'w', encoding='utf8').write(t); n += 1
print('rewrote', n, 'files')
