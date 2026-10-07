package slimeknights.tconstruct;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;
import slimeknights.mantle.platform.registry.DeferredRegister;
import slimeknights.mantle.platform.event.EventBus;
import slimeknights.mantle.platform.event.SubscribeEvent;
import net.fabricmc.loader.api.FabricLoader;
import slimeknights.mantle.platform.event.lifecycle.FMLCommonSetupEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import slimeknights.tconstruct.common.TinkerModule;
import slimeknights.tconstruct.common.TinkerTags;
import slimeknights.tconstruct.common.config.Config;
import slimeknights.tconstruct.common.network.TinkerNetwork;
import slimeknights.tconstruct.fluids.TinkerFluids;
import slimeknights.tconstruct.gadgets.TinkerGadgets;
import slimeknights.tconstruct.library.TinkerItemDisplays;
import slimeknights.tconstruct.library.materials.MaterialRegistry;
import slimeknights.tconstruct.library.tools.capability.TinkerDataCapability.ComputableDataKey;
import slimeknights.tconstruct.library.tools.capability.TinkerDataCapability.TinkerDataKey;
import slimeknights.tconstruct.library.tools.definition.ToolDefinitionLoader;
import slimeknights.tconstruct.library.tools.layout.StationSlotLayoutLoader;
import slimeknights.tconstruct.library.utils.Util;
import slimeknights.tconstruct.plugin.DietPlugin;
import slimeknights.tconstruct.plugin.DummmmmmyPlugin;
import slimeknights.tconstruct.plugin.ImmersiveEngineeringPlugin;
import slimeknights.tconstruct.plugin.craftingtweaks.CraftingTweaksPlugin;
import slimeknights.tconstruct.plugin.jsonthings.JsonThingsPlugin;
import slimeknights.tconstruct.shared.TinkerAttributes;
import slimeknights.tconstruct.shared.TinkerClient;
import slimeknights.tconstruct.shared.TinkerCommons;
import slimeknights.tconstruct.shared.TinkerEffects;
import slimeknights.tconstruct.shared.TinkerMaterials;
import slimeknights.tconstruct.smeltery.TinkerSmeltery;
import slimeknights.tconstruct.tables.TinkerTables;
import slimeknights.tconstruct.tools.TinkerModifiers;
import slimeknights.tconstruct.tools.TinkerToolParts;
import slimeknights.tconstruct.tools.TinkerTools;
import slimeknights.tconstruct.world.TinkerStructures;
import slimeknights.tconstruct.world.TinkerWorld;

import java.util.Locale;
import java.util.Random;
import java.util.function.Supplier;

/**
 * TConstruct, the tool mod. Craft your tools with style, then modify until the original is gone!
 *
 * @author mDiyo
 */

public class TConstruct implements ModInitializer {

  public static final String MOD_ID = "tconstruct";
  public static final Logger LOG = LogManager.getLogger(MOD_ID);
  public static final Random RANDOM = new Random();

  /* Instance of this mod, used for grabbing prototype fields */
  public static TConstruct instance;

  @Override
  public void onInitialize() {
    instance = this;

    Config.init();
    TinkerItemDisplays.init();
    MaterialRegistry.init();

    // initialize modules, done this way rather than with annotations to give us control over the order
    EventBus bus = EventBus.MOD_BUS;
    bus.register(TConstruct.class);
    // base
    bus.register(new TinkerCommons());
    bus.register(new TinkerMaterials());
    bus.register(new TinkerEffects());
    bus.register(new TinkerGadgets());
    bus.register(new TinkerAttributes());
    // world
    bus.register(new TinkerWorld());
    bus.register(new TinkerStructures());
    // tools
    bus.register(new TinkerTables());
    bus.register(new TinkerModifiers());
    bus.register(new TinkerToolParts());
    bus.register(new TinkerTools());
    // smeltery
    bus.register(new TinkerSmeltery());
    bus.register(new TinkerFluids());

    // init deferred registers
    TinkerModule.initRegisters();
    TinkerNetwork.setup();
    if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
      slimeknights.mantle.network.NetworkWrapper.registerClientReceivers();
    }
    TinkerTags.init();
    // init client logic
    if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
      TinkerClient.onConstruct();
    }

    // compat
    if (FabricLoader.getInstance().isModLoaded("immersiveengineering")) {
      bus.register(new ImmersiveEngineeringPlugin());
    }
    if (FabricLoader.getInstance().isModLoaded("jsonthings")) {
      JsonThingsPlugin.onConstruct();
    }
    if (FabricLoader.getInstance().isModLoaded("diet")) {
      DietPlugin.onConstruct();
    }
    if (FabricLoader.getInstance().isModLoaded("craftingtweaks")) {
      CraftingTweaksPlugin.onConstruct();
    }
    if (FabricLoader.getInstance().isModLoaded("dummmmmmy")) {
      bus.register(new DummmmmmyPlugin());
    }

    TinkerEventSubscribers.registerCommon();
    if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
      TinkerEventSubscribers.registerClient();
    }
    registerAliases();
    bus.post(new FMLCommonSetupEvent());
  }

  @SubscribeEvent
  static void commonSetup(final FMLCommonSetupEvent event) {
    ToolDefinitionLoader.init();
    StationSlotLayoutLoader.init();
  }

  /** Registers aliases for renamed or removed objects */
  private static void registerAliases() {
    alias(TinkerModule.BLOCKS, "silky_jewel_block", Blocks.EMERALD_BLOCK, "piglin_head", Blocks.PIGLIN_HEAD, "piglin_wall_head", Blocks.PIGLIN_WALL_HEAD);
    alias(TinkerModule.ITEMS, "silky_jewel", Items.EMERALD, "silky_jewel_block", Items.EMERALD_BLOCK, "piglin_head", Items.PIGLIN_HEAD);
    alias(TinkerModule.ITEMS, "round_plate", TinkerToolParts.adzeHead, "round_plate_cast", TinkerSmeltery.adzeHeadCast.get(),
          "round_plate_sand_cast", TinkerSmeltery.adzeHeadCast.getSand(), "round_plate_red_sand_cast", TinkerSmeltery.adzeHeadCast.getRedSand(),
          "slime_chestplate", TinkerTools.slimeWings);
  }

  /** Adds aliases from the old name to the new object, alternating name and target */
  @SuppressWarnings("unchecked")
  private static <T> void alias(DeferredRegister<T> register, Object... pairs) {
    Registry<T> registry = (Registry<T>) BuiltInRegistries.REGISTRY.get(register.getRegistryKey().location());
    for (int i = 0; i < pairs.length; i += 2) {
      Object target = pairs[i + 1];
      register.addAlias(getResource((String) pairs[i]), registry.getKey((T) (target instanceof Supplier<?> sup ? sup.get() : target)));
    }
  }

  /* Utils */

  /**
   * Gets a resource location for Tinkers
   * @param name  Resource path
   * @return  Location for tinkers
   */
  public static ResourceLocation getResource(String name) {
    return ResourceLocation.fromNamespaceAndPath(MOD_ID, name);
  }

  /**
   * Gets a data key for the capability, mainly used for modifier markers
   * @param name  Resource path
   * @return  Location for tinkers
   */
  public static <T> TinkerDataKey<T> createKey(String name) {
    return TinkerDataKey.of(getResource(name));
  }

  /**
   * Gets a data key for the capability, mainly used for modifier markers
   * @param name         Resource path
   * @param constructor  Constructor for compute if absent
   * @return  Location for tinkers
   */
  public static <T> ComputableDataKey<T> createKey(String name, Supplier<T> constructor) {
    return ComputableDataKey.of(getResource(name), constructor);
  }

  /**
   * Returns the given Resource prefixed with tinkers resource location. Use this function instead of hardcoding
   * resource locations.
   */
  public static String resourceString(String res) {
    return MOD_ID + ':' + res;
  }

  /**
   * Prefixes the given unlocalized name with tinkers prefix. Use this when passing unlocalized names for a uniform
   * namespace.
   */
  public static String prefix(String name) {
    return MOD_ID + "." + name.toLowerCase(Locale.US);
  }

  /** Makes a Tinker's description ID */
  public static String makeDescriptionId(String type, String name) {
    return type + "." + MOD_ID + "." + name;
  }

  /**
   * Makes a translation key for the given name
   * @param base  Base name, such as "block" or "gui"
   * @param name  Object name
   * @return  Translation key
   */
  public static String makeTranslationKey(String base, String name) {
    return Util.makeTranslationKey(base, getResource(name));
  }

  /**
   * Makes a translation text component for the given name
   * @param base  Base name, such as "block" or "gui"
   * @param name  Object name
   * @return  Translation key
   */
  public static MutableComponent makeTranslation(String base, String name) {
    return Component.translatable(makeTranslationKey(base, name));
  }

  /**
   * Makes a translation text component for the given name
   * @param base       Base name, such as "block" or "gui"
   * @param name       Object name
   * @param arguments  Additional arguments to the translation
   * @return  Translation key
   */
  public static MutableComponent makeTranslation(String base, String name, Object... arguments) {
    return Component.translatable(makeTranslationKey(base, name), arguments);
  }

  /**
   * This function is called in the constructor in some internal classes that are a common target for addons to wrongly extend.
   * These classes will cause issues if blindly used by the addon, and are typically trivial for the addon to implement
   * the parts they need if they just put in some effort understanding the code they are copying.
   *
   * As a reminder for addon devs, anything that is not in the library package can and will change arbitrarily. If you need to use a feature outside library, request it on our github.
   * @param self  Class to validate
   */
  public static void sealTinkersClass(Object self, String base, String solution) {
    // note for future maintainers: this does not use Java 9's sealed classes as unless you use modules those are restricted to the same package.
    // Dumb restriction but not like we can change it.
    String name = self.getClass().getName();
    if (!name.startsWith("slimeknights.tconstruct.")) {
      throw new IllegalStateException(base + " being extended from invalid package " + name + ". " + solution);
    }
  }
}
