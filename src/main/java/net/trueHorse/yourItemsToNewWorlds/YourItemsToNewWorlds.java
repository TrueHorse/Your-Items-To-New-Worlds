package net.trueHorse.yourItemsToNewWorlds;

import net.fabricmc.api.ModInitializer;

import net.trueHorse.yourItemsToNewWorlds.feature.YourItemsToNewWorldsFeatures;
import net.trueHorse.yourItemsToNewWorlds.network.C2SImportItemPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class YourItemsToNewWorlds implements ModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("your_items_to_new_worlds");

    @Override
    public void onInitialize() {
        YourItemsToNewWorldsFeatures.registerFeatures();
        C2SImportItemPacket.registerPacket();
    }
}