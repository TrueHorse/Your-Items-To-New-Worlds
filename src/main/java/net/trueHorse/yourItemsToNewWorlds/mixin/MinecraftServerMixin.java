package net.trueHorse.yourItemsToNewWorlds.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.storage.PrimaryLevelData;
import net.minecraft.world.level.storage.ServerLevelData;
import net.trueHorse.yourItemsToNewWorlds.YourItemsToNewWorlds;
import net.trueHorse.yourItemsToNewWorlds.duck.GeneratorOptionsAccess;
import net.trueHorse.yourItemsToNewWorlds.feature.ImportChestsFeature;
import net.trueHorse.yourItemsToNewWorlds.feature.YourItemsToNewWorldsFeatures;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;

@Mixin(MinecraftServer.class)
public class MinecraftServerMixin {

    @Inject(method = "setInitialSpawn",at=@At("TAIL"))
    private static void setupImportChests(ServerLevel world, ServerLevelData worldProperties, boolean bonusChest, boolean debugWorld, CallbackInfo ci){
        YourItemsToNewWorlds.LOGGER.error("injected");
        if(debugWorld) return;
        ArrayList<ItemStack> importItems = ((GeneratorOptionsAccess)((PrimaryLevelData)worldProperties).worldGenOptions()).getImportItems();
        if(!importItems.isEmpty()){
            ImportChestsFeature.importItems = importItems;
            YourItemsToNewWorldsFeatures.importChestsFeature.place(FeatureConfiguration.NONE,world, world.getChunkSource().getGenerator(), world.random, new BlockPos(worldProperties.getXSpawn(), worldProperties.getYSpawn(), worldProperties.getZSpawn()));
        }
    }
}
