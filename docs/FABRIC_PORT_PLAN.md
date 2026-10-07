# Fabric 1.21.1 port: triage and plan

## 1. Mantle gaps: what to do before Tinkers' Construct

| Gap | Needed for TC? | Decision |
|---|---|---|
| Client untested (models, fluid render, HUD) | Yes, TC leans on all of it | Test once a client can run; first real-world check is TC's first block/item models |
| Fluid fog / camera overlay not hooked | Visual polish only | After TC boots (mixin on FogRenderer / ScreenEffectRenderer) |
| Retextured/colored models ignore render type hints | Affects glass-like retextured blocks | After TC boots |
| NeoForge crit / sweep events not fired | TC modifiers need their own hooks anyway | Handle in TC's modifier hooks, not in Mantle |
| Loot injection pool names -> index | Only if TC data names pools | Check when porting TC data |
| Registry aliases only recorded | TC uses aliases for renames; harmless | Leave |
| Biome modifier command / tag `remove` lists | No | Leave dropped |
| Book export stencil | Dev tool only | Leave |
| Datagen not wired | Yes, TC's `src/generated` is 76 MB of datagen output | Step 1 below |

None of these block starting TC. The client verification is the main risk.

## 2. Tinkers' Construct sizing (measured, not guessed)

- Upstream has **no 1.21 TC branch** (only `1.19.2` and `1.20.1`). Mantle was a loader-only port because Mantle already had a NeoForge 1.21.1 version. TC is a **two-jump port**: Minecraft 1.20.1 -> 1.21.1 *and* Forge -> Fabric.
- 1938 Java files, 14 MB. 994 use Mantle APIs. About half (990) is `library` (modifiers 249, tools 180, recipes 175, client 138, json 131, materials 31).
- Probe (copy of `src/main/java` compiled against the Fabric Mantle jar, first pass only): **27,682 error lines in 1,214 files**. Mantle's first pass was 5,194 and hid roughly 3x more once imports resolved, so expect well over 100k raised errors in later passes.
- Beyond loader APIs, the 1.20 -> 1.21 jump changes TC's core: item NBT -> data components (`ToolStack` is NBT based), recipe serializers -> codecs/stream codecs, tooltips, enchantments as data, attributes, registries, networking.
- Mantle needed roughly a day of focused work for 589 files with a head start. TC is ~3x the files with no head start.

### Proposed approach
Vertical slices that always compile, using Gradle `sourceSets` excludes for packages not ported yet:
1. `library/utils`, `library/json`, `library/fluid`, `library/exception`, `common` (low dependency)
2. `fluids`, `shared` (registries, blocks, items)
3. `library/materials` + `library/modifiers` + `library/tools` (core: ToolStack onto data components)
4. `library/recipe`, `tools`, `smeltery`, `tables`, `world`, `gadgets`, `plugin`
5. Resources: copy `src/main/resources`, regenerate `src/generated` with Fabric datagen (50 MB + 76 MB)

### Open question for the repo owner
Is porting TC's 1.20.1 code to 1.21.1 Fabric the goal, or is there a 1.21 NeoForge TC (private/in-progress upstream) to start from? That halves the work if so.
