#!/usr/bin/env python3
"""
Migrates Forge capability usage to Mantle's capability shims (slimeknights.mantle.platform.capability.*).
  - imports (Capability, ICapabilityProvider, ICapabilitySerializable, LazyOptional, ForgeCapabilities -> Capabilities, util classes)
  - CapabilityManager.get(new CapabilityToken<>() {}) -> new Capability<>("name")
  - receiver.getCapability(args) -> Caps.get(receiver, args)      (not for super.getCapability / this-less declarations)
  - strips @Override from getCapability / initCapabilities / invalidateCaps / reviveCaps declarations
Idempotent. Usage: migrate_capabilities.py <java source dir>
"""
import os, re, sys

CAP = 'slimeknights.mantle.platform.capability.'
IMPORTS = {
  'net.minecraftforge.common.capabilities.Capability': CAP + 'Capability',
  'net.minecraftforge.common.capabilities.ICapabilityProvider': CAP + 'ICapabilityProvider',
  'net.minecraftforge.common.capabilities.ICapabilitySerializable': CAP + 'ICapabilitySerializable',
  'net.minecraftforge.common.util.LazyOptional': CAP + 'LazyOptional',
  'net.minecraftforge.common.capabilities.ForgeCapabilities': CAP + 'Capabilities',
  'net.minecraftforge.common.util.Lazy': 'slimeknights.mantle.platform.util.Lazy',
  'net.minecraftforge.common.util.NonNullConsumer': 'slimeknights.mantle.platform.util.NonNullConsumer',
  'net.minecraftforge.common.util.NonNullFunction': 'slimeknights.mantle.platform.util.NonNullFunction',
  'net.minecraftforge.common.util.NonNullSupplier': 'slimeknights.mantle.platform.util.NonNullSupplier',
  'net.minecraftforge.common.util.NonNullPredicate': 'slimeknights.mantle.platform.util.NonNullPredicate',
  'net.minecraftforge.event.AttachCapabilitiesEvent': 'slimeknights.mantle.platform.event.AttachCapabilitiesEvent',
}
imp = re.compile(r'^import (net\.minecraftforge\.[\w.]+);\n', re.M)

def find_receiver_start(text, dot):
    """index of the start of the expression ending right before text[dot] == '.'"""
    i = dot - 1
    while i >= 0:
        c = text[i]
        if c in ')]':
            close = c; open_ = '(' if c == ')' else '['
            depth = 0
            while i >= 0:
                if text[i] == close: depth += 1
                elif text[i] == open_:
                    depth -= 1
                    if depth == 0: break
                i -= 1
            i -= 1
        elif c == '>':
            # generic call like foo.<T>bar() is not expected; stop
            break
        elif c.isalnum() or c in '_.$':
            i -= 1
        else:
            break
    return i + 1

def rewrite_calls(text):
    out = []; pos = 0; changed = False
    pat = re.compile(r'\.getCapability\(')
    while True:
        m = pat.search(text, pos)
        if not m:
            out.append(text[pos:]); break
        dot = m.start()
        start = find_receiver_start(text, dot)
        recv = text[start:dot]
        if not recv or recv == 'super' or recv.endswith('.super') or recv in ('this',) and False:
            out.append(text[pos:m.end()]); pos = m.end(); continue
        out.append(text[pos:start])
        out.append('Caps.get(' + recv + ', ')
        pos = m.end()
        changed = True
    return ''.join(out), changed

def add_import(text, name):
    if f'import {name};' in text: return text
    return re.sub(r'^(package .*;\n)', r'\1\nimport ' + name + ';', text, count=1, flags=re.M)

def migrate(text):
    new = text
    renamed = False
    def repl(m):
        nonlocal renamed
        name = m.group(1)
        if name in IMPORTS:
            if name.endswith('ForgeCapabilities'): renamed = True
            return f'import {IMPORTS[name]};\n'
        return m.group(0)
    new = imp.sub(repl, new)
    if renamed:
        new = re.sub(r'\bForgeCapabilities\b', 'Capabilities', new)
    # capability tokens
    new = re.sub(r'(Capability<[^;=]+>\s+(\w+)\s*=\s*)CapabilityManager\.get\(new CapabilityToken<>\(\) \{\}\)',
                 lambda m: m.group(1) + 'new Capability<>("' + m.group(2).lower() + '")', new)
    new = re.sub(r'import net\.minecraftforge\.common\.capabilities\.(CapabilityManager|CapabilityToken);\n', '', new)
    new, c = rewrite_calls(new)
    if c:
        new = add_import(new, CAP + 'Caps')
    new = re.sub(r'@Override(\s+)((?:public|protected)\s+(?:<T>\s+)?(?:LazyOptional<T>|ICapabilityProvider|void)\s+(?:getCapability|initCapabilities|invalidateCaps|reviveCaps)\()', r'\1\2', new)
    return new, new != text

n = 0
for d, _, fs in os.walk(sys.argv[1]):
    for f in fs:
        if f.endswith('.java'):
            p = os.path.join(d, f)
            s = open(p, encoding='utf8').read()
            t, ch = migrate(s)
            if ch:
                open(p, 'w', encoding='utf8').write(t); n += 1
print('rewrote', n, 'files')
