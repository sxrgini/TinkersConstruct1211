#!/usr/bin/env python3
import re, pathlib
root = pathlib.Path("src/main/java")
P = "slimeknights.mantle.platform."
imports = {
  "net.minecraftforge.network.NetworkHooks": P + "PlatformHooks",
  "net.minecraftforge.entity.IEntityAdditionalSpawnData": P + "network.IEntityAdditionalSpawnData",
  "net.minecraftforge.event.entity.EntityAttributeCreationEvent": P + "event.entity.EntityAttributeCreationEvent",
  "net.minecraftforge.event.entity.EntityAttributeModificationEvent": P + "event.entity.EntityAttributeModificationEvent",
  "net.minecraftforge.event.entity.SpawnPlacementRegisterEvent.Operation": P + "event.entity.SpawnPlacementRegisterEvent.Operation",
  "net.minecraftforge.event.entity.SpawnPlacementRegisterEvent": P + "event.entity.SpawnPlacementRegisterEvent",
  "net.minecraftforge.event.entity.living.MobSpawnEvent.FinalizeSpawn": P + "event.living.MobSpawnEvent.FinalizeSpawn",
  "net.minecraftforge.event.entity.living.MobEffectEvent": P + "event.living.MobEffectEvent",
  "net.minecraftforge.event.entity.living.LivingEntityUseItemEvent": P + "event.living.LivingEntityUseItemEvent",
  "net.minecraftforge.event.level.BlockEvent.BreakEvent": P + "event.level.BlockEvent.BreakEvent",
  "net.minecraftforge.event.level.BlockEvent": P + "event.level.BlockEvent",
  "net.minecraftforge.event.RegisterCommandsEvent": P + "event.server.RegisterCommandsEvent",
  "net.minecraftforge.event.server.ServerStoppingEvent": P + "event.server.ServerStoppingEvent",
  "net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent": P + "capability.RegisterCapabilitiesEvent",
  "net.minecraftforge.common.TierSortingRegistry": P + "item.TierSortingRegistry",
  "net.minecraftforge.items.wrapper.SidedInvWrapper": P + "item.SidedInvWrapper",
  "net.minecraftforge.items.wrapper.EmptyHandler": P + "item.EmptyHandler",
  "net.minecraftforge.entity.PartEntity": "net.minecraft.world.entity.PartEntity",
  "net.minecraftforge.common.Tags.Items": P + "tags.Tags.Items",
}
n = 0
for f in root.rglob("*.java"):
  s = o = f.read_text()
  for a, b in imports.items():
    s = s.replace("import %s;" % a, "import %s;" % b)
  s = s.replace("NetworkHooks.", "PlatformHooks.")
  s = s.replace("SpawnPlacements.Type.", "SpawnPlacementTypes.")
  if "SpawnPlacementTypes." in s and "import net.minecraft.world.entity.SpawnPlacementTypes;" not in s:
    s = re.sub(r"(package [^\n]*\n\n)", r"\1import net.minecraft.world.entity.SpawnPlacementTypes;\n", s, count=1)
  # additional spawn data now uses the registry buffer
  if "IEntityAdditionalSpawnData" in s:
    s = re.sub(r"public void (write|read)SpawnData\(FriendlyByteBuf (\w+)\)", r"public void \1SpawnData(RegistryFriendlyByteBuf \2)", s)
    if "RegistryFriendlyByteBuf" in s and "import net.minecraft.network.RegistryFriendlyByteBuf;" not in s:
      s = re.sub(r"(package [^\n]*\n\n)", r"\1import net.minecraft.network.RegistryFriendlyByteBuf;\n", s, count=1)
    # vanilla spawns entities with the standard packet now, drop the Forge override
    s = re.sub(r"\n  @(Nonnull\n  @)?Override\n  public Packet<ClientGamePacketListener> getAddEntityPacket\(\) \{\n    return PlatformHooks\.getEntitySpawningPacket\(this\);\n  \}\n", "\n", s)
    s = re.sub(r"\n  @Override\n  public Packet<ClientGamePacketListener> getAddEntityPacket\(\) \{\n    return PlatformHooks\.getEntitySpawningPacket\(this\);\n  \}\n", "\n", s)
  if s != o:
    f.write_text(s); n += 1
print(n)
