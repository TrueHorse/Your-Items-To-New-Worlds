package net.trueHorse.yourItemsToNewWorlds.mixin.client;

import net.minecraft.client.gui.screens.worldselection.WorldCreationContext;
import net.minecraft.client.gui.screens.worldselection.WorldCreationUiState;
import net.minecraft.world.item.ItemStack;
import net.trueHorse.yourItemsToNewWorlds.duck.GeneratorOptionsAccess;
import net.trueHorse.yourItemsToNewWorlds.duck.WorldCreatorAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;

@Mixin(WorldCreationUiState.class)
public abstract class WorldCreatorMixin implements WorldCreatorAccess {

    @Shadow
    abstract public void update();
    @Shadow
    private WorldCreationContext generatorOptionsHolder;

    ArrayList<ItemStack> importItems = new ArrayList<>();

    public void setImportItems(ArrayList<ItemStack> importItems){
        this.importItems = importItems;
        update();
    }

    @Inject(method = "update",at=@At("TAIL"))
    private void updateImportItems(CallbackInfo ci){
        generatorOptionsHolder.withOptions(options -> ((GeneratorOptionsAccess)options).withImportItems(importItems));
    }
}
