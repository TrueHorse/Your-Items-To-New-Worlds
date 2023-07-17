package net.trueHorse.yourItemsToNewWorlds.mixin;

import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.feature.FeatureConfig;
import net.minecraft.world.level.LevelProperties;
import net.minecraft.world.level.ServerWorldProperties;
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

    @Inject(method = "setupSpawn",at=@At("TAIL"))
    private static void setupImportChests(ServerWorld world, ServerWorldProperties worldProperties, boolean bonusChest, boolean debugWorld, CallbackInfo ci){
        if(debugWorld) return;
        ArrayList<ItemStack> importItems = ((GeneratorOptionsAccess)((LevelProperties)worldProperties).getGeneratorOptions()).getImportItems();
        if(!importItems.isEmpty()){
            ImportChestsFeature.importItems = importItems;
            YourItemsToNewWorldsFeatures.importChestsFeature.generateIfValid(FeatureConfig.DEFAULT,world, world.getChunkManager().getChunkGenerator(), world.random, new BlockPos(worldProperties.getSpawnX(), worldProperties.getSpawnY(), worldProperties.getSpawnZ()));
        }
    }
}
