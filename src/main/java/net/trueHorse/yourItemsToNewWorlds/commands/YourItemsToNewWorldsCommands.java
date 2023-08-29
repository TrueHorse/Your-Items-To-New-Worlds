package net.trueHorse.yourItemsToNewWorlds.commands;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;

public class YourItemsToNewWorldsCommands {

    public static void registerClientCommands(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext buildContext){
        ImportItemsCommand.register(dispatcher,buildContext);
    }
}
