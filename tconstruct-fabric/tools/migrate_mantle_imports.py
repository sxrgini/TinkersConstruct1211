#!/usr/bin/env python3
"""Remaps TConstruct imports of Mantle 1.20 classes that moved in Mantle 1.21. Idempotent. Usage: migrate_mantle_imports.py <java dir>"""
import os, re, sys
M = 'slimeknights.mantle.'
MOVES = {
  'item.TooltipItem': 'item.tooltip.TooltipItem',
  'item.BlockTooltipItem': 'item.tooltip.BlockTooltipItem',
  'client.screen.SliderWidget': 'client.screen.widget.SliderWidget',
  'client.screen.Widget': 'client.screen.widget.Widget',
  'client.screen.TabsWidget': 'client.screen.widget.TabsWidget',
  'network.packet.BlockEntityPacket': 'network.BlockEntityPacket',
  'network.packet.ISimplePacket': 'network.ISimplePacket',
  'recipe.ingredient.PotionDisplayIngredient': 'recipe.ingredient.item.PotionDisplayIngredient',
  'recipe.ingredient.FluidContainerIngredient': 'recipe.ingredient.item.FluidContainerIngredient',
  'loot.condition.ContainsItemModifierLootCondition': 'loot.modifier.condition.ContainsItemModifierLootCondition',
  'loot.ReplaceItemLootModifier': 'loot.modifier.ReplaceItemLootModifier',
  'loot.AddEntryLootModifier': 'loot.modifier.AddEntryLootModifier',
  'loot.AbstractLootModifierBuilder': 'loot.modifier.AbstractLootModifierBuilder',
  'loot.LootTableInjection': 'loot.injection.LootTableInjection',
  'loot.AbstractLootTableInjectionProvider': 'loot.injection.AbstractLootTableInjectionProvider',
  'item.AbstractBookItem': 'item.book.AbstractBookItem',
  'data.loadable.common.RegistryLoadable': 'data.loadable.registry.RegistryLoadable',
  'recipe.ingredient.SizedIngredient': 'platform.ingredient.SizedIngredient',
  'recipe.ingredient.FluidIngredient': 'platform.fluid.crafting.FluidIngredient',
}
pat = re.compile(r'^import (static )?' + re.escape(M) + r'([\w.]+);\n', re.M)
def repl(m):
    name = m.group(2)
    for old, new in MOVES.items():
        if name == old or name.startswith(old + '.'):
            return f'import {m.group(1) or ""}{M}{new}{name[len(old):]};\n'
    return m.group(0)
n = 0
for d, _, fs in os.walk(sys.argv[1]):
    for f in fs:
        if f.endswith('.java'):
            p = os.path.join(d, f); s = open(p, encoding='utf8').read(); t = pat.sub(repl, s)
            if t != s: open(p, 'w', encoding='utf8').write(t); n += 1
print('rewrote', n, 'files')
