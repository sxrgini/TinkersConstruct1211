# Porting gaps and behavior changes

Notes recorded while porting to Fabric 1.21.1. Items here compile but behave differently or are not yet wired.

- `ModifiableArrow` / `ThrownTool`: vanilla knockback and pierce are enchantment based in 1.21, so `PunchModule` only affects `ModifiableArrow` and the "adder" overrides of `setPierceLevel`/`setKnockback` are gone.

## Hooks and mixins still needed

Forge-only hooks that no longer have an `@Override` target are listed in `porting-gaps-hooks.txt`. Behavior that needs a mixin before it works in game:

- **Dynamic tool attributes**: modifier attribute hooks (`AttributesModifierHook`) are not applied to held or worn tools, 1.21 reads attributes from the `ATTRIBUTE_MODIFIERS` component. Needs an `ItemStack` mixin around attribute iteration (`forEachModifier` / `getAttributeModifiers`).
- **Enchantment hooks**: modifier enchantment levels (`EnchantmentModifierHook`, `HarvestEnchantmentsModifierHook`) are only read by TC code. Applying them to vanilla enchantment checks needs an `ItemStack.getEnchantments` mixin. `EnchantmentModule.Protection` no longer cancels protection.
- **Looting**: approximated using the main hand enchantments, there is no looting level event.
- **Knockback / pierce adders** on projectiles were removed (enchantments are data driven now).
- **Item rarity, hide flags, tooltips**: the hide-flag tooltip suppression and rarity overrides are not wired.
- **First person / display context transforms**: `ToolModel.BakedToolModel.applyTransform` is no longer an override, per-display-context tool models need an item-render mixin.
- **Effect icon hooks**: modifier-provided effect icons are not wired.
- **Curative items**: `removeAllEffects()` replaces the curative item logic of Forge.
- **Brewing**: the glass bottle to slime/magma bottle brewing conversions and splash to lingering conversion are not registered, 1.21 only allows potion containers in the brewing registry. Needs a `PotionBrewing` mixin.

## Data and datagen

- Datagen is run with `./gradlew runDatagen` (Fabric datagen entrypoint `TConstructDataGenerator`), output is committed under `src/generated/resources`. Tag references to tags from other mods are not validated (`TagEntryMixin`, datagen only).
- Model, blockstate, item model, sprite and texture providers are **not** ported. The resulting 1.20 assets are taken from the repository root (`src/main/resources/assets`, `src/generated/resources/assets`) by `processResources`. These still need format migration for 1.21 (`forge:` parents and loaders, see below).
- `ConditionalRecipeBuilder` saves alternatives as separate recipes (`id`, `id_alt1`, ...) with the earlier conditions negated.
- `ConsumerWrapperBuilder.wrap(ResourceLocation)` ignores the serializer swap, which breaks the Ceramics compat wrappers.
- Tag providers: `remove` is not supported by vanilla tag providers, replacing is done through a local `TagData`.
- Fake registry entries for datagen unfreeze the registries (`FakeRegistryEntry`), only used while generating.
- `mantle:remove_recipes` presets and `generate_melting_recipes.json` are generated, but the `GenerateMeltingRecipesCommand` is not ported yet.
- Advancements and global loot modifiers use `neoforge:conditions`, applied by Mantle mixins (`ServerAdvancementManagerMixin`, `RecipeManagerMixin`). Loot tables themselves do not process conditions.
- Loot table IDs are not stored by vanilla, `LootTableIdCondition` uses a reverse lookup (`QueriedLootTable`).
- `HasEnchantmentEntityPredicate` now uses an enchantment key.
- `SizedIngredient` encodes the nested form (`ingredient` + `count`) when the ingredient is not an object, the flat form otherwise.

## Models and rendering (not yet tested)

- Custom model loaders (`mantle:connected`, `tconstruct:tool`, `tconstruct:fluid_container`, ...) keep their IDs, but the JSON still references `forge:item/default`, `forge:item/bucket_drip` and `forge:composite` parents/loaders. The client side has not been run yet.
- Plugins (JEI and mod integrations under `plugin/**`) are excluded from the build.
