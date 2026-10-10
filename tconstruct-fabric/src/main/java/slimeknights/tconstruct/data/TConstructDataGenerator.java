package slimeknights.tconstruct.data;

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.data.PackOutput;
import slimeknights.mantle.platform.data.ExistingFileHelper;
import slimeknights.tconstruct.common.data.DamageTypeProvider;
import slimeknights.tconstruct.common.data.advancement.FunctionProvider;
import slimeknights.tconstruct.common.data.loot.LootTableInjectionProvider;
import slimeknights.tconstruct.common.data.loot.TConstructLootTableProvider;
import slimeknights.tconstruct.common.data.tags.BiomeTagProvider;
import slimeknights.tconstruct.common.data.tags.EnchantmentTagProvider;
import slimeknights.tconstruct.common.data.tags.BlockEntityTypeTagProvider;
import slimeknights.tconstruct.common.data.tags.BlockTagProvider;
import slimeknights.tconstruct.common.data.tags.CreativeTabTagProvider;
import slimeknights.tconstruct.common.data.tags.DamageTypeTagProvider;
import slimeknights.tconstruct.common.data.tags.EnchantmentTagProvider;
import slimeknights.tconstruct.common.data.tags.EntityTypeTagProvider;
import slimeknights.tconstruct.common.data.tags.FluidTagProvider;
import slimeknights.tconstruct.common.data.tags.InstrumentTagProvider;
import slimeknights.tconstruct.common.data.tags.ItemTagProvider;
import slimeknights.tconstruct.common.data.tags.MaterialTagProvider;
import slimeknights.tconstruct.common.data.tags.MenuTypeTagProvider;
import slimeknights.tconstruct.common.data.tags.ModifierTagProvider;
import slimeknights.tconstruct.common.data.tags.PotionTagProvider;
import slimeknights.tconstruct.gadgets.data.GadgetRecipeProvider;
import slimeknights.tconstruct.shared.data.CommonRecipeProvider;
import slimeknights.tconstruct.smeltery.data.FluidContainerTransferProvider;
import slimeknights.tconstruct.smeltery.data.SmelteryRecipeProvider;
import slimeknights.tconstruct.tables.data.TableRecipeProvider;
import slimeknights.tconstruct.tools.data.EnchantmentToModifierProvider;
import slimeknights.tconstruct.tools.data.FluidEffectProvider;
import slimeknights.tconstruct.tools.data.ModifierProvider;
import slimeknights.tconstruct.tools.data.ModifierRecipeProvider;
import slimeknights.tconstruct.tools.data.StationSlotLayoutProvider;
import slimeknights.tconstruct.tools.data.ToolDefinitionDataProvider;
import slimeknights.tconstruct.tools.data.ToolsRecipeProvider;
import slimeknights.tconstruct.tools.data.material.MaterialDataProvider;
import slimeknights.tconstruct.tools.data.material.MaterialRecipeProvider;
import slimeknights.tconstruct.tools.data.material.MaterialStatsDataProvider;
import slimeknights.tconstruct.tools.data.material.MaterialTraitsDataProvider;
import slimeknights.tconstruct.tools.data.material.TrimMaterialProvider;
import slimeknights.tconstruct.world.data.MobEquipmentProvider;
import slimeknights.tconstruct.world.data.StructureRepalleter;
import slimeknights.tconstruct.world.data.WorldRecipeProvider;

import java.util.concurrent.CompletableFuture;

/**
 * Fabric data generator entrypoint, registers all server side data providers.
 * Model, blockstate and sprite generation is not ported, those assets are shipped as resources.
 */
public class TConstructDataGenerator implements DataGeneratorEntrypoint {
  @Override
  public void onInitializeDataGenerator(FabricDataGenerator generator) {
    FabricDataGenerator.Pack pack = generator.createPack();
    ExistingFileHelper helper = new ExistingFileHelper();

    // tags, all of these rely on the registry bootstrap in buildRegistry
    BlockTagProvider blockTags = pack.addProvider((output, registries) -> new BlockTagProvider(output, registries, helper));
    pack.addProvider((output, registries) -> new ItemTagProvider(output, registries, blockTags.contentsGetter(), helper));
    pack.addProvider((output, registries) -> new FluidTagProvider(output, registries, helper));
    pack.addProvider((output, registries) -> new EntityTypeTagProvider(output, registries, helper));
    pack.addProvider((output, registries) -> new BlockEntityTypeTagProvider(output, registries, helper));
    pack.addProvider((output, registries) -> new BiomeTagProvider(output, registries, helper));
    pack.addProvider((output, registries) -> new EnchantmentTagProvider(output, registries, helper));
    pack.addProvider((output, registries) -> new MenuTypeTagProvider(output, registries, helper));
    pack.addProvider((output, registries) -> new PotionTagProvider(output, registries, helper));
    pack.addProvider((output, registries) -> new CreativeTabTagProvider(output, registries, helper));
    pack.addProvider((output, registries) -> new InstrumentTagProvider(output, registries, helper));
    pack.addProvider((output, registries) -> new DamageTypeTagProvider(output, registries, helper));

    // loot and functions
    pack.addProvider(TConstructLootTableProvider::new);
    pack.addProvider((output, registries) -> new LootTableInjectionProvider(output, registries));
    pack.addProvider((output, registries) -> new FunctionProvider(output));

    // recipes
    pack.addProvider(CommonRecipeProvider::new);
    pack.addProvider(TableRecipeProvider::new);
    pack.addProvider(GadgetRecipeProvider::new);
    pack.addProvider(WorldRecipeProvider::new);
    pack.addProvider(SmelteryRecipeProvider::new);
    pack.addProvider(ToolsRecipeProvider::new);
    pack.addProvider(MaterialRecipeProvider::new);
    pack.addProvider(ModifierRecipeProvider::new);
    pack.addProvider((output, registries) -> new FluidContainerTransferProvider(output, registries));

    // tinkering data
    MaterialDataProvider materials = pack.addProvider((output, registries) -> new MaterialDataProvider(output));
    pack.addProvider((output, registries) -> new MaterialStatsDataProvider(output, materials));
    pack.addProvider((output, registries) -> new MaterialTraitsDataProvider(output, materials));
    pack.addProvider((output, registries) -> new ToolDefinitionDataProvider(output));
    pack.addProvider((output, registries) -> new StationSlotLayoutProvider(output));
    pack.addProvider((output, registries) -> new MaterialTagProvider(output, helper));
    pack.addProvider((output, registries) -> new ModifierProvider(output));
    pack.addProvider((output, registries) -> new FluidEffectProvider(output));
    pack.addProvider((output, registries) -> new ModifierTagProvider(output, helper));
    pack.addProvider((output, registries) -> new EnchantmentToModifierProvider(output));
    pack.addProvider((output, registries) -> new MobEquipmentProvider(output));
    pack.addProvider((output, registries) -> new StructureRepalleter(output, helper));
  }

  @Override
  public void buildRegistry(RegistrySetBuilder builder) {
    DamageTypeProvider.register(builder);
    TrimMaterialProvider.register(builder);
  }
}
