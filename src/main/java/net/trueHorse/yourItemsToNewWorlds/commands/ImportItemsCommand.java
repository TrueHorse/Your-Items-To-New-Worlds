package net.trueHorse.yourItemsToNewWorlds.commands;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.trueHorse.yourItemsToNewWorlds.gui.ImportItemsScreen;
import net.trueHorse.yourItemsToNewWorlds.network.YourItemsToNewWorldsPacketHandler;

public class ImportItemsCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext buildContext){
        dispatcher.register(Commands.literal("importItems").executes(context ->{
            Minecraft.getInstance().setScreen(new ImportItemsScreen(null,(items,screen)-> YourItemsToNewWorldsPacketHandler.INSTANCE.sendToServer(items)));
            return 1;
        }));
    }
}
