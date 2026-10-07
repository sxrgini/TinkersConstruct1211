package slimeknights.mantle.client;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.ChatFormatting;
import slimeknights.mantle.platform.client.ClientReloadListeners;
import slimeknights.mantle.platform.client.model.GeometryLoaders;
import net.minecraft.client.AttackIndicatorStatus;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import slimeknights.mantle.platform.capability.FluidHandlers;
import slimeknights.mantle.platform.fluid.FluidStack;
import slimeknights.mantle.platform.fluid.IFluidHandler;
import slimeknights.mantle.Mantle;
import slimeknights.mantle.block.GaugeBlock;
import slimeknights.mantle.client.book.BookLoader;
import slimeknights.mantle.client.book.repository.FileRepository;
import slimeknights.mantle.client.model.FallbackModelLoader;
import slimeknights.mantle.client.model.ItemKeyModel;
import slimeknights.mantle.client.model.RetexturedModel;
import slimeknights.mantle.client.model.TextureColorHelper;
import slimeknights.mantle.client.model.connected.ConnectedModel;
import slimeknights.mantle.client.render.ChannelFluids;
import slimeknights.mantle.client.render.FaucetFluid;
import slimeknights.mantle.client.render.MantleShaders;
import slimeknights.mantle.client.model.util.ColoredBlockModel;
import slimeknights.mantle.client.model.util.MantleItemLayerModel;
import slimeknights.mantle.client.model.util.ModelHelper;
import slimeknights.mantle.client.render.FluidCuboid;
import slimeknights.mantle.client.render.RenderItem;
import slimeknights.mantle.command.client.MantleClientCommand;
import slimeknights.mantle.datagen.MantleTags;
import slimeknights.mantle.fluid.texture.FluidTextureManager;
import slimeknights.mantle.fluid.tooltip.FluidTooltipHandler;
import slimeknights.mantle.registration.RegistrationHelper;
import slimeknights.mantle.util.OffhandCooldownTracker;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ClientEvents {
  /** Heart renderer, used by {@link slimeknights.mantle.mixin.GuiMixin} */
  public static final ExtraHeartRenderHandler HEARTS = new ExtraHeartRenderHandler();

  /** Registers all client logic, called from {@link MantleClient} */
  static void init() {
    // reload listeners
    ClientReloadListeners.register(Mantle.getResource("model_helper"), ModelHelper.LISTENER);
    ClientReloadListeners.register(Mantle.getResource("books"), new BookLoader());
    ResourceColorManager.init();
    FluidTooltipHandler.init();
    FluidTextureManager.init();
    ClientReloadListeners.register(Mantle.getResource("fluid_cuboids"), FluidCuboid.REGISTRY);
    ClientReloadListeners.register(Mantle.getResource("render_items"), RenderItem.REGISTRY);
    ClientReloadListeners.register(Mantle.getResource("texture_colors"), TextureColorHelper.RELOAD_LISTENER);
    ChannelFluids.initialize();
    FaucetFluid.initialize();
    MantleShaders.init();

    // wood types, must be added to the sheet maps before signs render
    RegistrationHelper.forEachWoodType(type -> {
      Sheets.SIGN_MATERIALS.put(type, Sheets.createSignMaterial(type));
      Sheets.HANGING_SIGN_MATERIALS.put(type, Sheets.createHangingSignMaterial(type));
    });

    BookLoader.registerBook(Mantle.getResource("test"), new FileRepository(Mantle.getResource("books/test")));
    MantleClientCommand.init();

    // model loaders
    // standard models - useful in resource packs for any model
    GeometryLoaders.register(ConnectedModel.ID, ConnectedModel.LOADER);
    GeometryLoaders.register(MantleItemLayerModel.ID, MantleItemLayerModel.LOADER);
    GeometryLoaders.register(ColoredBlockModel.ID, ColoredBlockModel.LOADER);
    GeometryLoaders.register(FallbackModelLoader.ID, FallbackModelLoader.INSTANCE);
    // NBT dynamic models - require specific data defined in the block/item to use
    GeometryLoaders.register(ItemKeyModel.ID, ItemKeyModel.LOADER);
    GeometryLoaders.register(RetexturedModel.ID, RetexturedModel.LOADER);

    // HUD
    HudRenderCallback.EVENT.register((graphics, tickDelta) -> {
      renderOffhandAttackIndicator(graphics, false);
      renderOffhandAttackIndicator(graphics, true);
      renderGaugeTooltip(graphics);
    });
  }

  /** Renders the offhand attack indicator. Based on {@link Gui#renderCrosshair(GuiGraphics, DeltaTracker)} and {@link Gui#renderItemHotbar(GuiGraphics, DeltaTracker)} */
  private static void renderOffhandAttackIndicator(GuiGraphics graphics, boolean isHotbar) {
    // must have a player, not be in spectator, and have the indicator enabled
    Minecraft minecraft = Minecraft.getInstance();
    Options settings = minecraft.options;
    AttackIndicatorStatus indicator = settings.attackIndicator().get();
    if (minecraft.player == null || minecraft.gameMode == null || minecraft.gameMode.getPlayerMode() == GameType.SPECTATOR || indicator == AttackIndicatorStatus.OFF) {
      return;
    }

    // fetch the current cooldown
    OffhandCooldownTracker tracker = OffhandCooldownTracker.get(minecraft.player);
    float cooldown = tracker.getCooldown();
    if (cooldown >= 1.0f) {
      return;
    }

    // show attack indicator
    switch (indicator) {
      case CROSSHAIR:
        if (!isHotbar && minecraft.options.getCameraType().isFirstPerson()) {
          if (!minecraft.gui.getDebugOverlay().showDebugScreen() || minecraft.player.isReducedDebugInfo() || settings.reducedDebugInfo().get()) {
            // mostly cloned from vanilla attack indicator
            RenderSystem.enableBlend();
            RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.ONE_MINUS_DST_COLOR, GlStateManager.DestFactor.ONE_MINUS_SRC_COLOR, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
            int scaledHeight = minecraft.getWindow().getGuiScaledHeight();
            // integer division makes this a pain to line up, there might be a simplier version of this formula, but I cannot think of one
            int y = (scaledHeight / 2) - 14 + (2 * (scaledHeight % 2));
            int x = minecraft.getWindow().getGuiScaledWidth() / 2 - 8;
            int width = (int)(cooldown * 17.0F);
            graphics.blitSprite(Gui.CROSSHAIR_ATTACK_INDICATOR_BACKGROUND_SPRITE, x, y, 16, 4);
            graphics.blitSprite(Gui.CROSSHAIR_ATTACK_INDICATOR_PROGRESS_SPRITE, 16, 4, 0, 0, x, y, width, 4);
            RenderSystem.defaultBlendFunc();
          }
        }
        break;
      case HOTBAR:
        if (isHotbar && minecraft.cameraEntity == minecraft.player) {
          int centerWidth = minecraft.getWindow().getGuiScaledWidth() / 2;
          int y = minecraft.getWindow().getGuiScaledHeight() - 20;
          int x;
          // opposite of the vanilla hand location, extra bit to offset past the offhand slot
          if (minecraft.player.getMainArm() == HumanoidArm.RIGHT) {
            x = centerWidth - 91 - 22 - 32;
          } else {
            x = centerWidth + 91 + 6 + 32;
          }
          int height = (int)(cooldown * 19.0F);
          graphics.blitSprite(Gui.HOTBAR_ATTACK_INDICATOR_BACKGROUND_SPRITE, x, y, 18, 18);
          graphics.blitSprite(Gui.HOTBAR_ATTACK_INDICATOR_PROGRESS_SPRITE, 18, 18, 0, 18 - height, x, y + 18 - height, 18, height);
        }
        break;
    }
  }



  /** Renders the tooltip when targeting the gauge block */
  private static void renderGaugeTooltip(GuiGraphics graphics) {
    // must not be in a screen, though chat is fine
    Minecraft minecraft = Minecraft.getInstance();
    if (minecraft.screen != null && minecraft.screen.getClass() != ChatScreen.class) {
      return;
    }
    // must have a hit result
    if (minecraft.level == null || minecraft.hitResult == null || minecraft.hitResult.getType() != HitResult.Type.BLOCK) {
      return;
    }
    BlockHitResult blockHit = (BlockHitResult) minecraft.hitResult;
    BlockPos pos = blockHit.getBlockPos();

    // must be targeting a gauge
    BlockState targeted = minecraft.level.getBlockState(blockHit.getBlockPos());
    if (!targeted.is(MantleTags.Blocks.GAUGES)) {
      return;
    }
    BlockState fluidBlock = targeted;
    BlockPos fluidPos = pos;
    Direction side = blockHit.getDirection();
    if (targeted.is(MantleTags.Blocks.ATTACHED_GAUGES)) {
      if (targeted.hasProperty(BlockStateProperties.FACING)) {
        side = targeted.getValue(BlockStateProperties.FACING);
      }
      fluidPos = pos.relative(side.getOpposite());
      fluidBlock = minecraft.level.getBlockState(fluidPos);
    } else {
      side = blockHit.getDirection();
    }
    // targeted block must not be blacklisted
    if (fluidBlock.is(MantleTags.Blocks.GAUGE_BLACKLIST)) {
      return;
    }
    // block entity must have a fluid handler
    IFluidHandler handler = FluidHandlers.getBlock(minecraft.level, fluidPos, side);
    if (handler == null || handler.getTanks() <= 0) {
      return;
    }
    // if the fluid is empty, just render the capacity
    FluidStack fluid = handler.getFluidInTank(0);
    List<Component> tooltip;
    if (fluid.isEmpty()) {
      tooltip = List.of(GaugeBlock.formatCapacity(handler.getTankCapacity(0)));
    } else if (fluidBlock.is(MantleTags.Blocks.HIDES_GAUGE_AMOUNT)) {
      // in the tag, don't show capacity
      ResourceLocation id = BuiltInRegistries.FLUID.getKey(fluid.getFluid());
      tooltip = new ArrayList<>(3);
      tooltip.add(fluid.getHoverName());
      FluidTooltipHandler.appendAdvanced(id, tooltip);
      tooltip.add(GaugeBlock.formatCapacity(handler.getTankCapacity(0)).withStyle(ChatFormatting.GRAY));
      tooltip.add(FluidTooltipHandler.formatModName(id));
    } else {
      // render full fluid tooltip
      tooltip = FluidTooltipHandler.getFluidTooltip(fluid);
    }

    int x = minecraft.getWindow().getGuiScaledWidth() / 2;
    int y = minecraft.getWindow().getGuiScaledHeight() / 2;
    graphics.renderTooltip(minecraft.font, tooltip, Optional.empty(), x, y);
  }
}
