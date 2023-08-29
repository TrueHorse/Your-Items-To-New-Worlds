package net.trueHorse.yourItemsToNewWorlds.network;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

import java.util.ArrayList;

public class C2SImportItemPacketClient {

    private static final Identifier IMPORT_ITEMS_CHANNEL = new Identifier("your_items_to_new_worlds","import_items");

    public static void sendPacket(ArrayList<ItemStack> stacks){
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeCollection(stacks, PacketByteBuf::writeItemStack);
        ClientPlayNetworking.send(IMPORT_ITEMS_CHANNEL,buf);
    }
}
