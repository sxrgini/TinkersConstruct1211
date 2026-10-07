# Mantle Fabric port: status

Source: SlimeKnights/Mantle branch `1.21` (NeoForge 21.1.238, MC 1.21.1), copied as-is, then the build was swapped to Fabric Loom.

## Done
- Loom build (`build.gradle`, `gradle.properties`, `settings.gradle`), `fabric.mod.json` template.
- NeoForge AT kept as `accesstransformer.neoforge.reference.cfg` for conversion to an access widener.

## NOT done / unverified
- Nothing has been compiled: maven.fabricmc.net etc. were blocked by the sandbox network policy.
- Fabric versions in gradle.properties are unverified.
- ~154 of 589 source files still import `net.neoforged.*` and will not compile.

## Port order
1. Access widener (needs field descriptors; generate with Loom once online)
2. `registration/*` (DeferredRegister -> direct Registry.register)
3. `fluid/*` (capabilities/IFluidHandler -> Fabric Transfer API)
4. `network/*` (NeoForge payloads -> Fabric networking)
5. `client/*` (model loaders, fluid textures -> Fabric rendering API)
6. `loot/modifier`, `recipe/ingredient`, `recipe/condition`, `datagen`, `command`
