package net.trueHorse.yourItemsToNewWorlds.mixin.client;

import net.minecraft.item.ItemStack;

import java.util.ArrayList;

public interface WorldCreatorAccess {

    void setImportItems(ArrayList<ItemStack> importItems);
}
