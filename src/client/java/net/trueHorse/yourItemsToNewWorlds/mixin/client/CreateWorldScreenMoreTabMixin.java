package net.trueHorse.yourItemsToNewWorlds.mixin.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.world.CreateWorldScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.GridWidget;
import net.minecraft.text.Text;
import net.trueHorse.yourItemsToNewWorlds.YourItemsToNewWorlds;
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

    @Inject(method = "<init>", at = @At("TAIL"), locals = LocalCapture.CAPTURE_FAILSOFT)
    private void addItemTransferButton(CreateWorldScreen createWorldScreen, CallbackInfo ci, GridWidget.Adder adder) {
        adder.add(ButtonWidget.builder(Text.of("Transfertest"), button -> MinecraftClient.getInstance().setScreen(new ImportItemsScreen(field_42178, list -> YourItemsToNewWorlds.LOGGER.warn("apply")))).width(210).build());
    }

}
