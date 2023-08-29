package net.trueHorse.yourItemsToNewWorlds.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraftforge.network.NetworkEvent;
import net.trueHorse.yourItemsToNewWorlds.feature.ImportChestsFeature;
import net.trueHorse.yourItemsToNewWorlds.feature.YourItemsToNewWorldsFeatures;

import java.util.ArrayList;
import java.util.function.Supplier;

public class C2SImportItemsPacket {

    public static void encode (ArrayList<ItemStack> stacks, FriendlyByteBuf byteBuf){
        byteBuf.writeCollection(stacks,(buf,itemStack)->buf.writeItemStack(itemStack,false));
    }

    public static ArrayList<ItemStack> decode(FriendlyByteBuf byteBuf){
        return byteBuf.readCollection(ArrayList::new, FriendlyByteBuf::readItem);
    }

    public static void handleMessage(ArrayList<ItemStack> stacks, Supplier<NetworkEvent.Context> supplier){
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(()->{
            ImportChestsFeature.importItems = stacks;
            ServerLevel level = (ServerLevel) context.getSender().level();
            YourItemsToNewWorldsFeatures.importChestsFeature.place(FeatureConfiguration.NONE,level,level.getChunkSource().getGenerator(),level.random,context.getSender().blockPosition());
        });
        context.setPacketHandled(true);
    }
}
