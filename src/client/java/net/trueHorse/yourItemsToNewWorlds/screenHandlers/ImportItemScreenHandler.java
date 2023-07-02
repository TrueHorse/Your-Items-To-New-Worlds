package net.trueHorse.yourItemsToNewWorlds.screenHandlers;

import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;

public class ImportItemScreenHandler {

    private ArrayList<ItemStack> importableItemStacks;
    private boolean[] itemSelected;

    public void initImportableItemStacksWith(ArrayList<ItemStack> itemStacks){
        importableItemStacks = itemStacks;
        itemSelected = new boolean[itemStacks.size()];
        Arrays.fill(itemSelected,false);
    }

    public ArrayList<ItemStack> getImportableItems() {
        return importableItemStacks;
    }


    public void toggleSelection(int index){
        itemSelected[index] = !itemSelected[index];
    }

    public boolean[] getItemSelected() {
        return itemSelected;
    }
}
