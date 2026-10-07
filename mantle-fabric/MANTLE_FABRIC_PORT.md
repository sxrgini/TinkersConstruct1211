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

## Ported so far (platform shims in `slimeknights.mantle.platform.*`)
- `registry`: DeferredRegister/DeferredHolder/DeferredItem/DeferredBlock over Fabric `Registry.register` (call `register()` in the mod initializer).
- `fluid`: FluidStack (mB), IFluidHandler(+Item). Bridge to Fabric Transfer API (droplets = mB * 81) still TODO.
- `condition`: ICondition/IContext/True/False/Not + ConditionRegistry (Mantle's own load conditions; Fabric `fabric:load_conditions` bridge for recipes TODO).
- `network`: IPayloadContext/IPayloadHandler/PacketDistributor; `MantleNetwork` rewritten for Fabric networking (client handlers via `registerClientHandlers`).
- build: jsr305 + errorprone compileOnly (NeoForge provided these transitively).

## Still NeoForge-only (design work needed)
- FluidType / BaseFlowingFluid / FluidIngredient / SizedFluidIngredient (`fluid/*`, `registration/deferred/FluidDeferredRegister`, `recipe/ingredient/fluid`)
- Item ingredients: ICustomIngredient / IngredientType / SizedIngredient -> Fabric CustomIngredient
- Capabilities (Capabilities.FluidHandler, IItemHandler) -> Transfer API
- Client models: geometry loaders, ModelData, SimpleBlockModel/ConnectedModel/RetexturedModel/ColoredBlockModel/MantleItemLayerModel -> Fabric rendering API
- Events/bus/FML (`Mantle`, `MantleEvents`, `ClientEvents`), config (ModConfigSpec), datagen providers, global loot modifiers, attachments, InvertedFluid

## Port order
1. Access widener (needs field descriptors; generate with Loom once online)
2. `registration/*` (DeferredRegister -> direct Registry.register)
3. `fluid/*` (capabilities/IFluidHandler -> Fabric Transfer API)
4. `network/*` (NeoForge payloads -> Fabric networking)
5. `client/*` (model loaders, fluid textures -> Fabric rendering API)
6. `loot/modifier`, `recipe/ingredient`, `recipe/condition`, `datagen`, `command`
