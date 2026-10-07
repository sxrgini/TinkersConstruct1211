package slimeknights.tconstruct.tables.network;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import slimeknights.mantle.platform.network.IPayloadContext;
import slimeknights.mantle.network.BlockEntityPacket;
import slimeknights.mantle.network.packet.IThreadsafePacket;
import slimeknights.mantle.recipe.helper.RecipeHelper;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.library.recipe.tinkerstation.ITinkerStationRecipe;
import slimeknights.tconstruct.tables.block.entity.table.TinkerStationBlockEntity;
import slimeknights.tconstruct.tables.client.inventory.TinkerStationScreen;

/**
 * Packet to send the current crafting recipe to a player who opens the tinker station.
 * TODO 1.21: make record.
 */
public class UpdateTinkerStationRecipePacket implements IThreadsafePacket {
  private final BlockPos pos;
  private final ResourceLocation recipe;
  public UpdateTinkerStationRecipePacket(BlockPos pos, net.minecraft.world.item.crafting.RecipeHolder<ITinkerStationRecipe> recipe) {
    this.pos = pos;
    this.recipe = recipe.id();
  }

  public UpdateTinkerStationRecipePacket(RegistryFriendlyByteBuf buffer) {
    this.pos = buffer.readBlockPos();
    this.recipe = buffer.readResourceLocation();
  }

  @Override
  public void encode(RegistryFriendlyByteBuf buffer) {
    buffer.writeBlockPos(pos);
    buffer.writeResourceLocation(recipe);
  }

  @Override
  public void handleThreadsafe(IPayloadContext context) {
    HandleClient.handle(this);
  }

  /** Safely runs client side only code in a method only called on client */
  private static class HandleClient {
    private static void handle(UpdateTinkerStationRecipePacket packet) {
      Minecraft mc = Minecraft.getInstance();
      Level world = mc.level;
      if (world != null) {
        // start by fetching the recipe, no further work if it's missing
        @SuppressWarnings("unchecked")
        net.minecraft.world.item.crafting.RecipeHolder<ITinkerStationRecipe> recipe = world.getRecipeManager().byKey(packet.recipe).filter(h -> h.value() instanceof ITinkerStationRecipe).map(h -> (net.minecraft.world.item.crafting.RecipeHolder<ITinkerStationRecipe>) h).orElse(null);
        if (recipe == null) {
          TConstruct.LOG.error("Failed to update Tinker Station Recipe at {}: unknown recipe {}", packet.pos, packet.recipe);
          return;
        }
        // if the screen is open, use that to get the TE and update the screen as we want to update the screen too
        if (mc.screen instanceof TinkerStationScreen stationScreen) {
          TinkerStationBlockEntity te = stationScreen.getTileEntity();
          if (te != null && te.getBlockPos().equals(packet.pos)) {
            te.updateRecipe(recipe);
            stationScreen.updateDisplay();
            return;
          }
        }
        // if the wrong screen is open or no screen, use the tile directly
        if (BlockEntityPacket.getBlockEntity(world, packet.pos, packet) instanceof TinkerStationBlockEntity be) {
          be.updateRecipe(recipe);
        } else {
          TConstruct.LOG.error("Failed to update Tinker Station Recipe at {} to {}: unable to find Tinker Station", packet.pos, packet.recipe);
        }
      }
    }
  }
}
