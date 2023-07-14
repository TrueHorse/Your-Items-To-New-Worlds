package net.trueHorse.yourItemsToNewWorlds.mixin;

import net.minecraft.item.ItemStack;
import net.minecraft.world.gen.GeneratorOptions;

import java.util.ArrayList;

public interface GeneratorOptionsAccess {
    GeneratorOptions withImportItems(ArrayList<ItemStack> importItems);
}
