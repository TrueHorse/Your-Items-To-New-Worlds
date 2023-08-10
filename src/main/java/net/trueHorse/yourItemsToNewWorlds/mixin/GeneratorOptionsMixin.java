package net.trueHorse.yourItemsToNewWorlds.mixin;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.trueHorse.yourItemsToNewWorlds.duck.GeneratorOptionsAccess;
import org.spongepowered.asm.mixin.Mixin;

import java.util.ArrayList;

@Mixin(WorldOptions.class)
public class GeneratorOptionsMixin implements GeneratorOptionsAccess {

    private ArrayList<ItemStack> importItems = new ArrayList<>();

    public WorldOptions withImportItems(ArrayList<ItemStack> importItems){
        this.importItems = importItems;
        return ((WorldOptions)(Object)this);
    }

    public ArrayList<ItemStack> getImportItems(){
        return importItems;
    }
}
