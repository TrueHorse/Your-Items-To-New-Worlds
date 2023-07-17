package net.trueHorse.yourItemsToNewWorlds.mixin;

import net.minecraft.item.ItemStack;
import net.minecraft.world.gen.GeneratorOptions;
import net.trueHorse.yourItemsToNewWorlds.duck.GeneratorOptionsAccess;
import org.spongepowered.asm.mixin.Mixin;

import java.util.ArrayList;

@Mixin(GeneratorOptions.class)
public class GeneratorOptionsMixin implements GeneratorOptionsAccess {

    private ArrayList<ItemStack> importItems = new ArrayList<>();

    public GeneratorOptions withImportItems(ArrayList<ItemStack> importItems){
        this.importItems = importItems;
        return ((GeneratorOptions)(Object)this);
    }

    public ArrayList<ItemStack> getImportItems(){
        return importItems;
    }
}
