package net.trueHorse.yourItemsToNewWorlds.command;

import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;

public class YourItemsToNewWorldsClientCommands {

    public static void registerAll(){
        ClientCommandRegistrationCallback.EVENT.register(ImportItemsCommand::register);
    }
}
