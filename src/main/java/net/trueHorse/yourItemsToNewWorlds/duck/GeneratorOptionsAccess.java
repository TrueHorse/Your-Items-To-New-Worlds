package net.trueHorse.yourItemsToNewWorlds.duck;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.levelgen.WorldOptions;

import java.util.ArrayList;

public interface GeneratorOptionsAccess {
    WorldOptions withImportItems(ArrayList<ItemStack> importItems);
    ArrayList<ItemStack> getImportItems();
}
