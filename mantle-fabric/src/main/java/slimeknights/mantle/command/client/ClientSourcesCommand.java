package slimeknights.mantle.command.client;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.client.Minecraft;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.resources.ResourceLocation;
import slimeknights.mantle.command.SourcesCommand;

import java.util.ArrayList;
import java.util.List;

/** Command to list all sources for a file in a resource pack */
public class ClientSourcesCommand {
  /** List of subcommands to add */
  private static final List<ClientSourceFolder> FOLDERS = new ArrayList<>();

  /** Folder to add a command for */
  private record ClientSourceFolder(String argument, String folder, String extension, SuggestionProvider<FabricClientCommandSource> suggestionProvider) {}

  /** Registers this command with the builder */
  public static void register(LiteralArgumentBuilder<FabricClientCommandSource> subCommand) {
    subCommand.then(net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal("path")
      .then(net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument("path", ResourceLocationArgument.id())
        .executes(context -> runClient(context, Minecraft.getInstance().getResourceManager(), context.getArgument("path", ResourceLocation.class)))));
    for (ClientSourceFolder source : FOLDERS) {
      subCommand.then(net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal(source.argument())
        .then(net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument("id", ResourceLocationArgument.id()).suggests(source.suggestionProvider())
          .executes(context -> run(context, source.folder(), context.getArgument("id", ResourceLocation.class), source.extension()))));
    }
  }

  /** Runs for the given ID and resource manager, sending the result as client feedback */
  private static int runClient(CommandContext<FabricClientCommandSource> context, net.minecraft.server.packs.resources.ResourceManager manager, ResourceLocation path) throws CommandSyntaxException {
    java.util.List<String> packs = manager.getResourceStack(path).stream().map(net.minecraft.server.packs.resources.Resource::sourcePackId).toList();
    if (packs.isEmpty()) {
      throw SourcesCommand.NOT_FOUND.create(path);
    }
    net.minecraft.network.chat.MutableComponent component = net.minecraft.network.chat.Component.translatableEscape("command.mantle.sources.success", path);
    for (String pack : packs) {
      component = component.append(net.minecraft.network.chat.Component.literal("\n* " + (pack.isEmpty() ? "<unnamed>" : pack)));
    }
    context.getSource().sendFeedback(component);
    return packs.size();
  }

  /** Runs for the given folder and extension */
  private static int run(CommandContext<FabricClientCommandSource> context, String folder, ResourceLocation id, String extension) throws CommandSyntaxException {
    return runClient(context, Minecraft.getInstance().getResourceManager(), id.withPath(folder + '/' + id.getPath() + extension));
  }


  /* Registering interesting folders */

  /** Suggests values using the passed suggestion provider */
  public static void register(String argument, String folder, String extension, SuggestionProvider<FabricClientCommandSource> suggestionProvider) {
    FOLDERS.add(new ClientSourceFolder(argument, folder, extension, suggestionProvider));
  }

  public static void registerMinecraft(String folder, SuggestionProvider<FabricClientCommandSource> suggestionProvider) {
    register(folder, folder, ".json", suggestionProvider);
  }
}
