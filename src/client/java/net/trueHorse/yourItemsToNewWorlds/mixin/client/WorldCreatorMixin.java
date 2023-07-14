package net.trueHorse.yourItemsToNewWorlds.mixin.client;

import net.minecraft.client.gui.screen.world.WorldCreator;
import net.minecraft.client.world.GeneratorOptionsHolder;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKey;
import net.minecraft.world.gen.WorldPreset;
import net.trueHorse.yourItemsToNewWorlds.mixin.GeneratorOptionsAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Optional;
import java.util.OptionalLong;

@Mixin(WorldCreator.class)
public class WorldCreatorMixin extends WorldCreator implements WorldCreatorAccess{

    ArrayList<ItemStack> importItems = new ArrayList<>();

    public WorldCreatorMixin(Path savesDirectory, GeneratorOptionsHolder generatorOptionsHolder, Optional<RegistryKey<WorldPreset>> defaultWorldType, OptionalLong seed) {
        super(savesDirectory, generatorOptionsHolder, defaultWorldType, seed);
    }

    public void setImportItems(ArrayList<ItemStack> importItems){
        this.importItems = importItems;
        update();
    }

    @Inject(method = "update",at=@At("TAIL"))
    private void updateImportItems(CallbackInfo ci){
        this.getGeneratorOptionsHolder().apply(options -> ((GeneratorOptionsAccess)options).withImportItems(importItems));
    }
}
