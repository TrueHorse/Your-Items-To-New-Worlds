package net.trueHorse.yourItemsToNewWorlds.duck;

import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;

public interface WorldCreatorAccess {

    void setImportItems(ArrayList<ItemStack> importItems);
}
