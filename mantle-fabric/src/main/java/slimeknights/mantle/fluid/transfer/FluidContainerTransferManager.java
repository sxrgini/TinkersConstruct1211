package slimeknights.mantle.fluid.transfer;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import lombok.Setter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.PackType;
import slimeknights.mantle.util.DataLoadedConditionContext;
import slimeknights.mantle.platform.condition.ICondition;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import slimeknights.mantle.platform.fluid.FluidStack;
import slimeknights.mantle.Mantle;
import slimeknights.mantle.data.loadable.field.ContextKey;
import slimeknights.mantle.network.PacketHelper;
import slimeknights.mantle.util.JsonHelper;
import slimeknights.mantle.util.typed.TypedMap;
import slimeknights.mantle.util.typed.TypedMapBuilder;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Consumer;

/** Logic for filling and emptying fluid containers that are not fluid handlers */
public class FluidContainerTransferManager extends SimpleJsonResourceReloadListener implements IdentifiableResourceReloadListener {
  /** Folder for saving the logic */
  public static final String FOLDER = "mantle/fluid_transfer";
  /** Singleton instance of the manager */
  public static final FluidContainerTransferManager INSTANCE = new FluidContainerTransferManager();

  /** List of loaded transfer logic, only exists serverside */
  private List<IFluidContainerTransfer> transfers = Collections.emptyList();

  /** Set of all items that match a recipe, exists on both sides */
  @Setter @Nullable
  private Set<Item> containerItems = Collections.emptySet();

  private FluidContainerTransferManager() {
    super(JsonHelper.DEFAULT_GSON, FOLDER);
  }

  /** Lazily initializes the set of container items */
  protected Set<Item> getContainerItems() {
    if (this.containerItems == null) {
      List<Item> builder = new ArrayList<>();
      Consumer<Item> consumer = builder::add;
      for (IFluidContainerTransfer transfer : transfers) {
        transfer.addRepresentativeItems(consumer);
      }
      this.containerItems = Set.copyOf(builder);
    }
    return this.containerItems;
  }

  /** For internal use only */
  public void init() {
    ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(this);
    // registries are not available during the reload, so parsing is deferred until all data has loaded
    ServerLifecycleEvents.SERVER_STARTED.register(server -> parse(server.registryAccess()));
    ServerLifecycleEvents.END_DATA_PACK_RELOAD.register((server, manager, success) -> {
      if (success) {
        parse(server.registryAccess());
      }
    });
    ServerLifecycleEvents.SYNC_DATA_PACK_CONTENTS.register((player, joined) -> PacketHelper.sendStaticRegistry(player, new FluidContainerTransferPacket(this.getContainerItems())));
  }

  @Override
  public ResourceLocation getFabricId() {
    return Mantle.getResource("fluid_container_transfer");
  }

  /** JSON loaded during the reload, parsed once registries are available */
  private Map<ResourceLocation,JsonElement> pending = Collections.emptyMap();

  @Override
  protected void apply(Map<ResourceLocation,JsonElement> splashList, ResourceManager manager, ProfilerFiller profiler) {
    this.pending = splashList;
  }

  /** Parses the loaded JSON into transfers */
  private void parse(RegistryAccess registries) {
    Map<ResourceLocation,JsonElement> splashList = this.pending;
    long time = System.nanoTime();
    ICondition.IContext conditionContext = DataLoadedConditionContext.INSTANCE;
    TypedMap context = TypedMapBuilder.builder().put(ContextKey.REGISTRY_LOOKUP, registries).put(ContextKey.CONDITION_CONTEXT, conditionContext).build();
    List<IFluidContainerTransfer> transfers = new ArrayList<>(splashList.size());
    for (Entry<ResourceLocation, JsonElement> entry : splashList.entrySet()) {
      ResourceLocation key = entry.getKey();
      try {
        JsonObject json = GsonHelper.convertToJsonObject(entry.getValue(), key.toString());
        if (JsonHelper.processConditions(json, "conditions", conditionContext)) {
          transfers.add(IFluidContainerTransfer.LOADER.deserialize(json, context));
        }
      } catch (JsonSyntaxException e) {
        Mantle.logger.error("Failed to load fluid container transfer info from {}", key, e);
      }
    }
    this.transfers = List.copyOf(transfers);
    this.containerItems = null;
    Mantle.logger.info("Loaded {} fluid container transfers in {} ms", transfers.size(), (System.nanoTime() - time) / 1000000f);
  }

  /**
   * Checks if the given stack could possibly match, used client side to determine if the fluid transfer falls back to opening the UI
   * @param item  Item to check
   * @return  True if a match is possible, basically just checks item ID
   */
  public boolean mayHaveTransfer(ItemLike item) {
    return getContainerItems().contains(item.asItem());
  }

  /**
   * Checks if the given stack could possibly match, used client side to determine if the fluid transfer falls back to opening the UI
   * @param stack  Stack to check
   * @return  True if a match is possible, basically just checks item ID
   */
  public boolean mayHaveTransfer(ItemStack stack) {
    return getContainerItems().contains(stack.getItem());
  }

  /** Gets the transfer for the given item and fluid, or null if its not a valid item and fluid */
  @Nullable
  public IFluidContainerTransfer getTransfer(ItemStack stack, FluidStack fluid) {
    for (IFluidContainerTransfer transfer : transfers) {
      if (transfer.matches(stack, fluid)) {
        return transfer;
      }
    }
    return null;
  }
}
