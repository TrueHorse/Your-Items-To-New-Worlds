package net.trueHorse.yourItemsToNewWorlds.mixin;

import net.minecraft.item.ItemStack;
import net.minecraft.world.gen.GeneratorOptions;
import org.spongepowered.asm.mixin.Mixin;

import java.util.ArrayList;

@Mixin(GeneratorOptions.class)
public class GenerationOptionsMixin extends GeneratorOptions implements GeneratorOptionsAccess{

    ArrayList<ItemStack> importItems = new ArrayList<>();

    public GenerationOptionsMixin(long seed, boolean generateStructures, boolean bonusChest) {
        super(seed, generateStructures, bonusChest);
    }

    public GeneratorOptions withImportItems(ArrayList<ItemStack> importItems){
        this.importItems = importItems;
        return this;
    }
}
