# Mantle Fabric port: status

Source: SlimeKnights/Mantle branch `1.21` (NeoForge 21.1.238, MC 1.21.1), copied as-is, then the build was swapped to Fabric Loom.

## Done
- Loom build (`build.gradle`, `gradle.properties`, `settings.gradle`), `fabric.mod.json` template.
- NeoForge AT kept as `accesstransformer.neoforge.reference.cfg` for conversion to an access widener.

- Toolchain verified: `./gradlew compileJava` resolves Minecraft 1.21.1 + Parchment + Fabric API and runs javac.
  Pinned: Loom 1.15.5 (newer Loom needs Gradle 9.5 / Java 25), Fabric API 0.116.0+1.21.1 (0.116.1x were built with Loom 1.18.2), JEI 19.51.0.418 (newer JEI is also built with a newer Loom).

## NOT done
- Source is partly ported. javac errors: 5194 -> 2742 (428 files). Many remaining errors cascade from the unported areas below.
- Most frequent missing packages: neoforge.registries, neoforge.fluids(+capability/crafting), neoforge.client.model.geometry, neoforge.common.conditions, neoforge.common.data, neoforge.bus.api (events), neoforge.network.handling, neoforge.client.model.generators.
- Access widener not yet written (reference AT in accesstransformer.neoforge.reference.cfg).
- Maven Central via the sandbox proxy intermittently returns 429; just retry.

## Status
- `./gradlew build` succeeds: 0 compile errors, remapped jar produced.
- **Dedicated server boots** (`./gradlew runServer`, MC 1.21.1, Loader 0.19.5, Fabric API 0.116.5, JEI 19.51 in dev): entrypoint, mixins, access widener, mantle registries, loot modifiers, fluid container transfers all load with no errors from Mantle.
- **Client is untested** (no display here): model loaders, fluid rendering, HUD, shaders, commands are compiled only.
- Build pins: Loom 1.15.5, Fabric API 0.116.5+1.21.1, JEI 19.51.0.418 (newer versions need Loom 1.18 / Java 25). Maven Central via the sandbox proxy sometimes returns 429, just retry.

## Ported (platform shims in `slimeknights.mantle.platform.*`)
- `registry`: DeferredRegister/DeferredHolder (a `Holder<R>` like NeoForge)/DeferredItem/DeferredBlock over `Registry.register`; call `register()` in the mod initializer.
- `fluid`: FluidStack (mB), IFluidHandler(+Item), FluidType/FluidTypes (own registry `mantle:fluid_type`, vanilla water/lava/empty registered), FluidTypeProvider, BaseFlowingFluid, SoundAction(s), fluid ingredients (`fluid.crafting`).
- `capability` + `item`: FluidHandlers/FluidStorageAdapter and ItemHandlers bridge the Fabric Transfer API (droplets = mB * 81); IItemHandler, InvWrapper, SlotItemHandler, ItemAbility(+Provider/Abilities), ItemHelpers.
- `ingredient`: ICustomIngredient on Fabric CustomIngredient, IngredientType, SizedIngredient.
- `condition`: ICondition/IContext/True/False/Not + ConditionRegistry (ids `mantle:*`, `neoforge:*` accepted as aliases).
- `network`: IPayloadContext/IPayloadHandler/PacketDistributor; `MantleNetwork` on Fabric networking.
- `loot`: LootModifier/IGlobalLootModifier + GlobalLootModifierManager (`data/*/loot_modifiers/*.json`, applied by `LootTableMixin`).
- `menu`: IContainerFactory + MenuTypes (ExtendedScreenHandlerType).
- `client`: IClientFluidTypeExtensions (registers Fabric fluid render handlers), SpriteHelper, ClientReloadListeners.
- `client.model`: geometry loaders via `GeometryLoaders` (Fabric ModelLoadingPlugin reading the NeoForge style `"loader"` key from model JSON), IGeometryBakingContext, ModelData bridged to `RenderDataBlockEntity`, BakedModelWrapper (Fabric renderer API bridge), QuadTransformers, CompositeModel, QuadBakingVertexConsumer, TransformingVertexPipeline.
- `data`: ExistingFileHelper (reads loaded mods), BlockTagsProvider; `client.model.generators`: minimal ModelBuilder/CustomLoaderBuilder.
- `Mantle` is a `ModInitializer`, `MantleClient` the client entrypoint; config is JSON at `config/mantle.json`; soulbound uses Fabric events + `InventoryMixin`; hearts use `GuiMixin`; HUD via `HudRenderCallback`; shaders via `CoreShaderRegistrationCallback`.

## Behavior differences / known gaps
- Biome modifier removal command dropped (no Fabric equivalent). Tag command no longer supports tag `remove` lists (vanilla/Fabric tags have none).
- CombatHelper does not fire NeoForge critical-hit / sweep-attack events.
- Loot injection pool names: vanilla pools have no names, `main` / `pool<N>` map to pool index.
- Registry aliases (DeferredRegister.addAlias) are recorded only.
- Render types: retextured/colored models ignore NeoForge render type hints; Fabric uses the block's registered layer.
- Fluid fog colour/shape modifiers and the camera overlay from `IClientFluidTypeExtensions` are not hooked into rendering yet; textures and tint are.
- `BookCommand` image export no longer enables the stencil buffer (needs a Fabric mixin); `PoseStack`/`Transformation` helpers are approximations of NeoForge's.
- Datagen is compiled but not wired to a Fabric `DataGeneratorEntrypoint`; model builders only emit the custom loader JSON.
- Recipe JSON conditions use Fabric `fabric:load_conditions`; Mantle data uses its own `conditions` key.

## Port order
1. Access widener (needs field descriptors; generate with Loom once online)
2. `registration/*` (DeferredRegister -> direct Registry.register)
3. `fluid/*` (capabilities/IFluidHandler -> Fabric Transfer API)
4. `network/*` (NeoForge payloads -> Fabric networking)
5. `client/*` (model loaders, fluid textures -> Fabric rendering API)
6. `loot/modifier`, `recipe/ingredient`, `recipe/condition`, `datagen`, `command`
