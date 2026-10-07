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

## Status: javac errors 5194 -> 474 (80 files). All remaining errors are client rendering or datagen.

## Ported so far (platform shims in `slimeknights.mantle.platform.*`)
- `registry`: DeferredRegister/DeferredHolder/DeferredItem/DeferredBlock over Fabric `Registry.register` (call `register()` in the mod initializer).
- `fluid`: FluidStack (mB), IFluidHandler(+Item), FluidType/FluidTypes (own registry `mantle:fluid_type`), FluidTypeProvider, BaseFlowingFluid, SoundAction(s), fluid ingredients (`fluid.crafting`).
- `capability`: FluidHandlers + FluidStorageAdapter bridge Fabric Transfer API (droplets = mB * 81) to IFluidHandler(+Item).
- `item`: IItemHandler(+Modifiable), InvWrapper, SlotItemHandler, ItemHandlers (Transfer API bridge), ItemAbility(+Provider/Abilities).
- `ingredient`: ICustomIngredient on Fabric CustomIngredient, IngredientType, SizedIngredient.
- `condition`: ICondition/IContext/True/False/Not + ConditionRegistry (Mantle's own load conditions; Fabric `fabric:load_conditions` bridge for recipes TODO).
- `network`: IPayloadContext/IPayloadHandler/PacketDistributor; `MantleNetwork` on Fabric networking (client handlers via `registerClientHandlers`, TODO: call from client initializer, and set `PacketDistributor.setClientSender`).
- `loot`: LootModifier/IGlobalLootModifier + GlobalLootModifierManager (loads `data/*/loot_modifiers/*.json`, applied by `LootTableMixin`).
- `menu`: IContainerFactory + MenuTypes (ExtendedScreenHandlerType based).
- `Mantle` is a `ModInitializer`; config is JSON at `config/mantle.json`; soulbound uses Fabric death/copy events + `InventoryMixin`; access widener generated from the old AT; loot injection and fluid transfer parsing run after data load via Fabric lifecycle events; stripping and sign blocks registered via Fabric APIs.
- Behavior differences to know: biome modifier removal command dropped (no Fabric equivalent); critical hit/sweep NeoForge events are not fired in CombatHelper; `ConditionalOps` removed (conditions are processed explicitly).

## Still NeoForge-only
- Client: model loaders/geometry (`client/model/*`, SimpleBlockModel, ConnectedModel, RetexturedModel, ColoredBlockModel, MantleItemLayerModel, ItemKeyModel, FallbackModelLoader), ModelData (`RetexturedHelper`, `DefaultRetexturedBlockEntity`), `ClientEvents`, shaders, extra hearts, `ClientTextureFluidType` (fluid rendering), book/screens bits.
- Datagen: tag providers, `GenericTextureGenerator`, fluid texture/transfer providers, model builders. Needs Fabric datagen entrypoint (`fabric-datagen-api-v1`) and rewrites to `FabricTagProvider`.
- Not wired yet: client initializer, `fabric.mod.json` client entrypoint, `PacketDistributor.setClientSender`, Fabric `fabric:load_conditions` for recipe JSON.

## Port order
1. Access widener (needs field descriptors; generate with Loom once online)
2. `registration/*` (DeferredRegister -> direct Registry.register)
3. `fluid/*` (capabilities/IFluidHandler -> Fabric Transfer API)
4. `network/*` (NeoForge payloads -> Fabric networking)
5. `client/*` (model loaders, fluid textures -> Fabric rendering API)
6. `loot/modifier`, `recipe/ingredient`, `recipe/condition`, `datagen`, `command`
