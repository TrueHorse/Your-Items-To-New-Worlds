package net.trueHorse.yourItemsToNewWorlds.network;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import net.trueHorse.yourItemsToNewWorlds.YourItemsToNewWorlds;

import java.util.ArrayList;

public class YourItemsToNewWorldsPacketHandler {
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(YourItemsToNewWorlds.MODID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    public static void registerPackets(){
        INSTANCE.registerMessage(0, ArrayList.class,C2SImportItemsPacket::encode,C2SImportItemsPacket::decode,C2SImportItemsPacket::handleMessage);
    }
}
