package net.trueHorse.yourItemsToNewWorlds.feature;

import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.world.gen.feature.DefaultFeatureConfig;
import net.minecraft.world.gen.feature.Feature;

public class YourItemsToNewWorldsFeatures {

    public static Feature<DefaultFeatureConfig> importChestsFeature = new ImportChestsFeature(DefaultFeatureConfig.CODEC);
    public static void registerFeatures(){
        Registry.register(Registries.FEATURE,new Identifier("your_items_to_new_worlds","import_chests"),importChestsFeature);
    }
}
