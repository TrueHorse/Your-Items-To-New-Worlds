package net.trueHorse.yourItemsToNewWorlds.mixin.client;

import net.minecraft.client.gui.screen.world.WorldCreator;
import net.minecraft.client.world.GeneratorOptionsHolder;
import net.minecraft.item.ItemStack;
import net.trueHorse.yourItemsToNewWorlds.duck.GeneratorOptionsAccess;
import net.trueHorse.yourItemsToNewWorlds.duck.WorldCreatorAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;

@Mixin(WorldCreator.class)
public abstract class WorldCreatorMixin implements WorldCreatorAccess {

    @Shadow
    abstract public void update();
    @Shadow
    private GeneratorOptionsHolder generatorOptionsHolder;

    ArrayList<ItemStack> importItems = new ArrayList<>();

    public void setImportItems(ArrayList<ItemStack> importItems){
        this.importItems = importItems;
        update();
    }

    @Inject(method = "update",at=@At("TAIL"))
    private void updateImportItems(CallbackInfo ci){
        generatorOptionsHolder.apply(options -> ((GeneratorOptionsAccess)options).withImportItems(importItems));
    }
}
