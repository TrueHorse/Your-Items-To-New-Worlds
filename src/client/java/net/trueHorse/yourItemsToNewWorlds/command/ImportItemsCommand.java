package net.trueHorse.yourItemsToNewWorlds.command;

import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandRegistryAccess;
import net.trueHorse.yourItemsToNewWorlds.gui.ImportItemsScreen;
import net.trueHorse.yourItemsToNewWorlds.network.C2SImportItemPacketClient;

public class ImportItemsCommand {

    public static boolean justExecuted;

    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandRegistryAccess registryAccess){
        dispatcher.register(ClientCommandManager.literal("importItems").executes(context ->{
            context.getSource().getClient().setScreen(new ImportItemsScreen(null,(items, screen)-> C2SImportItemPacketClient.sendPacket(items)));
            justExecuted = true;
            return 1;
        }));
    }
}
