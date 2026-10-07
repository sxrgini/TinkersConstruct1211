package slimeknights.mantle.platform.data;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.IntrinsicHolderTagsProvider;
import net.minecraft.world.level.block.Block;

import javax.annotation.Nullable;
import java.util.concurrent.CompletableFuture;

/** Block tag provider for mod tags, replacing NeoForge's {@code BlockTagsProvider}. */
public abstract class BlockTagsProvider extends IntrinsicHolderTagsProvider<Block> {
  protected BlockTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, String modId, @Nullable ExistingFileHelper existingFileHelper) {
    super(output, Registries.BLOCK, lookupProvider, block -> BuiltInRegistries.BLOCK.getResourceKey(block).orElseThrow());
  }
}
