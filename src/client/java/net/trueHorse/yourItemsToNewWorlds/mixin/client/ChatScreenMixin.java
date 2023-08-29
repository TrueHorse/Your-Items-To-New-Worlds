package net.trueHorse.yourItemsToNewWorlds.mixin.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.Screen;
import net.trueHorse.yourItemsToNewWorlds.command.ImportItemsCommand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ChatScreen.class)
public class ChatScreenMixin {

    @Redirect(method = "keyPressed",at = @At(value = "INVOKE",target = "Lnet/minecraft/client/MinecraftClient;setScreen(Lnet/minecraft/client/gui/screen/Screen;)V",ordinal = 1))
    private void nullScreenIfNotItemImport(MinecraftClient instance, Screen screen){
        if(!ImportItemsCommand.justExecuted){
            instance.setScreen(screen);
        }
    }
}
