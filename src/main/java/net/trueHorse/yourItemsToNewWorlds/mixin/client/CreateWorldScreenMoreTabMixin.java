package net.trueHorse.yourItemsToNewWorlds.mixin.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.network.chat.Component;
import net.trueHorse.yourItemsToNewWorlds.duck.WorldCreatorAccess;
import net.trueHorse.yourItemsToNewWorlds.gui.ImportItemsScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(targets = "net/minecraft/client/gui/screen/world/CreateWorldScreen$MoreTab")
public class CreateWorldScreenMoreTabMixin {

    @Shadow
    private CreateWorldScreen field_42178;
    private ImportItemsScreen oldImportItemScreen = null;

    @Inject(method = "<init>", at = @At("TAIL"), locals = LocalCapture.CAPTURE_FAILSOFT)
    private void addItemTransferButton(CreateWorldScreen createWorldScreen, CallbackInfo ci, GridLayout.RowHelper adder) {
        adder.addChild(Button.builder(Component.translatable("selectWorld.your_items_to_new_worlds.import_items"),
                button -> Minecraft.getInstance().setScreen(oldImportItemScreen!=null ? oldImportItemScreen : new ImportItemsScreen(field_42178,
                (importItems,closedScreen) -> {
                    ((WorldCreatorAccess)field_42178.getUiState()).setImportItems(importItems);
                    oldImportItemScreen = closedScreen;
                }))).width(210).build());
    }

}
