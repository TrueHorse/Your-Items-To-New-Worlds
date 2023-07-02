package net.trueHorse.yourItemsToNewWorlds.screenHandlers;

import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ImportItemScreenHandler {

    private ArrayList<ItemStack> importableItemStacks;
    private List<Boolean> itemSelected;

    public void initImportableItemStacksWith(ArrayList<ItemStack> itemStacks){
        importableItemStacks = itemStacks;
        itemSelected = Collections.nCopies(itemStacks.size(),false);
    }

    public ArrayList<ItemStack> getImportableItems() {
        return importableItemStacks;
    }

    public List<Boolean> getItemSelected() {
        return itemSelected;
    }
}
