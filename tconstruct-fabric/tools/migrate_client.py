#!/usr/bin/env python3
import re, pathlib
root = pathlib.Path("src/main/java")
C = "slimeknights.mantle.platform.event.client."
imports = {
  "net.minecraftforge.client.event.RegisterColorHandlersEvent": C + "RegisterColorHandlersEvent",
  "net.minecraftforge.client.event.EntityRenderersEvent": C + "EntityRenderersEvent",
  "net.minecraftforge.client.event.ModelEvent.RegisterGeometryLoaders": C + "ModelEvent.RegisterGeometryLoaders",
  "net.minecraftforge.client.event.ModelEvent.RegisterAdditional": C + "ModelEvent.RegisterAdditional",
  "net.minecraftforge.client.event.ModelEvent": C + "ModelEvent",
  "net.minecraftforge.client.event.RegisterParticleProvidersEvent": C + "RegisterParticleProvidersEvent",
  "net.minecraftforge.client.event.RegisterKeyMappingsEvent": C + "RegisterKeyMappingsEvent",
  "net.minecraftforge.client.event.RenderHandEvent": C + "RenderHandEvent",
  "net.minecraftforge.client.event.RenderGuiOverlayEvent": C + "RenderGuiOverlayEvent",
  "net.minecraftforge.client.gui.overlay.VanillaGuiOverlay": C + "RenderGuiOverlayEvent.VanillaGuiOverlay",
  "net.minecraftforge.client.event.RenderLevelStageEvent.Stage": C + "RenderLevelStageEvent.Stage",
  "net.minecraftforge.client.event.RenderLevelStageEvent": C + "RenderLevelStageEvent",
  "net.minecraftforge.client.event.RenderHighlightEvent": C + "RenderHighlightEvent",
  "net.minecraftforge.client.event.RenderBlockScreenEffectEvent": C + "RenderBlockScreenEffectEvent",
  "net.minecraftforge.client.event.RenderLivingEvent": C + "RenderLivingEvent",
  "net.minecraftforge.client.event.RenderNameTagEvent": C + "RenderNameTagEvent",
  "net.minecraftforge.client.event.RenderItemInFrameEvent": C + "RenderItemInFrameEvent",
  "net.minecraftforge.client.event.ComputeFovModifierEvent": C + "ComputeFovModifierEvent",
  "net.minecraftforge.client.event.MovementInputUpdateEvent": C + "MovementInputUpdateEvent",
  "net.minecraftforge.client.event.InputEvent": C + "InputEvent",
  "net.minecraftforge.client.event.ClientPlayerNetworkEvent.LoggingOut": C + "ClientPlayerNetworkEvent.LoggingOut",
  "net.minecraftforge.client.event.ClientPlayerNetworkEvent": C + "ClientPlayerNetworkEvent",
  "net.minecraftforge.client.event.RecipesUpdatedEvent": C + "RecipesUpdatedEvent",
  "net.minecraftforge.event.entity.player.ItemTooltipEvent": "slimeknights.mantle.platform.event.player.ItemTooltipEvent",
}
n = 0
for f in root.rglob("*.java"):
  s = o = f.read_text()
  for a, b in imports.items():
    s = s.replace("import %s;" % a, "import %s;" % b)
  if s != o:
    f.write_text(s); n += 1
print(n)
