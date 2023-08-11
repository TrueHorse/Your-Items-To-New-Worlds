package net.trueHorse.yourItemsToNewWorlds.feature;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraftforge.registries.ForgeRegistries;

public class YourItemsToNewWorldsFeatures {

    public static Feature<NoneFeatureConfiguration> importChestsFeature = new ImportChestsFeature(NoneFeatureConfiguration.CODEC);
    public static void registerFeatures(){
        ForgeRegistries.FEATURES.register(new ResourceLocation("your_items_to_new_worlds","import_chests"),importChestsFeature);
    }
}
