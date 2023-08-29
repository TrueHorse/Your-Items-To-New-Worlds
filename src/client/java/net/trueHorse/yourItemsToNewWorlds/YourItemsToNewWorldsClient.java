package net.trueHorse.yourItemsToNewWorlds;

import net.fabricmc.api.ClientModInitializer;
import net.trueHorse.yourItemsToNewWorlds.command.YourItemsToNewWorldsClientCommands;

public class YourItemsToNewWorldsClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        YourItemsToNewWorldsClientCommands.registerAll();
    }
}
