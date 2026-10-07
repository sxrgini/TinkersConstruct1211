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

### Import graph finding (changes the approach)
Slicing by package does not work: **1,106 of the 1,938 files form one strongly connected import cluster** (modifiers 201, tools library 131, recipes 100, tool modules 86, json 77, client 75, smeltery blocks 63, ...). The core cannot compile green piece by piece, so the TC port follows the Mantle method: copy everything in, then drive the compile error count down.

### Current state (`tconstruct-fabric/`)
- All 1,938 Java files copied from `src/main/java`; resources (`src/main/resources`, `src/generated`) are not copied yet.
- `tools/migrate_imports.py`: idempotent script applying 1:1 Forge -> Mantle shim import mappings (FluidStack, IFluidHandler, ICondition, ModelData, geometry loaders, ExistingFileHelper, item handlers, ToolAction -> ItemAbility, ...). First run rewrote 360 files; Forge imports 1,469 -> 904.
- Baseline compile: **23,710 error lines in 1,151 files** (first pass only; later passes will surface more).
- Build: `cd mantle-fabric && ./gradlew jar`, then `cd tconstruct-fabric && ./gradlew compileJava`.

### Work that cannot be scripted (needs design)
1. **Events**: ~200 Forge event usages (`MinecraftForge.EVENT_BUS`, `@SubscribeEvent`, LivingHurt/BreakSpeed/EntityTeleport/PlayerInteract/...). Fabric has far fewer events, so many need mixins; this is where most TC mechanics (modifier hooks) live.
2. **Capabilities** (`LazyOptional`, `ForgeCapabilities`, `ICapabilityProvider`, `AttachCapabilitiesEvent`): tool/part/tank capabilities need Fabric Transfer API registrations, item components and attachments.
3. **Registries**: `RegistryObject<T>` -> Mantle 1.21 `DeferredHolder<R,T>` / `ItemObject<R,I>` (Mantle's own API changed between 1.20 and 1.21, so TC's registration objects need adapting).
4. **1.20 -> 1.21 data model**: tool/item NBT -> data components (`ToolStack`), recipe serializers -> `MapCodec` + `StreamCodec`, tooltips, enchantments, attributes, networking (`NetworkEvent.Context` -> payloads).
5. **Tags**: `net.minecraftforge.common.Tags` -> Fabric convention tags (`c:` namespace).
6. **Config**: `ForgeConfigSpec` -> Mantle-style JSON config.
7. **Resources**: 1.21 renamed data pack folders to singular (`recipe`, `loot_table`, `tags/item`, ...) and changed recipe/result formats, so the 126 MB of JSON needs a migration script, then Fabric datagen.
8. **Plugins**: JEI (Forge -> Fabric artifacts), CraftingTweaks, JsonThings, ImmersiveEngineering.
