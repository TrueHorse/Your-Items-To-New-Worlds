package net.trueHorse.yourItemsToNewWorlds.network;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.world.gen.feature.FeatureConfig;
import net.trueHorse.yourItemsToNewWorlds.feature.ImportChestsFeature;
import net.trueHorse.yourItemsToNewWorlds.feature.YourItemsToNewWorldsFeatures;

import java.util.ArrayList;

public class C2SImportItemPacket {

    private static final Identifier IMPORT_ITEMS_CHANNEL = new Identifier("your_items_to_new_worlds","import_items");

    public static void registerPacket(){
        ServerPlayNetworking.registerGlobalReceiver(IMPORT_ITEMS_CHANNEL, (server, player, handler, buf, sender) -> {
            ArrayList<ItemStack> stacks = buf.readCollection(ArrayList::new, PacketByteBuf::readItemStack);
            server.execute(()->{
                ImportChestsFeature.importItems = stacks;
                ServerWorld world = player.getServerWorld();
                YourItemsToNewWorldsFeatures.importChestsFeature.generateIfValid(FeatureConfig.DEFAULT,world,world.getChunkManager().getChunkGenerator(),world.random,player.getBlockPos());
            });
        });
    }
}
