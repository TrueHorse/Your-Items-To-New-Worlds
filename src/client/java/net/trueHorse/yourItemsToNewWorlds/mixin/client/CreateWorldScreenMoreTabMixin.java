package net.trueHorse.yourItemsToNewWorlds.mixin.client;

import net.minecraft.client.gui.screen.world.CreateWorldScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.GridWidget;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtList;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.trueHorse.yourItemsToNewWorlds.YourItemsToNewWorlds;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.io.File;
import java.io.IOException;

@Mixin(targets = "net/minecraft/client/gui/screen/world/CreateWorldScreen$MoreTab")
public class CreateWorldScreenMoreTabMixin {

    @Inject(method = "<init>", at = @At("TAIL"),locals = LocalCapture.CAPTURE_FAILSOFT)
    private void addItemTransferButton(CreateWorldScreen createWorldScreen, CallbackInfo ci, GridWidget.Adder adder){
        adder.add(ButtonWidget.builder(Text.of("Transfertest"),button -> printItemsFromOtherWorld()).width(210).build());
    }

    private void printItemsFromOtherWorld(){
        String path = "C:\\Users\\Paul\\curseforge\\minecraft\\Instances\\Spectrum\\saves\\New World";
        String uuid = "2c143ece-4173-4b31-97ca-bd6c2458fc3a";

        NbtCompound nbtCompound = null;

        File file = new File(path+"\\playerdata", uuid + ".dat");
        if (file.exists() && file.isFile()) {
            try {
                nbtCompound = NbtIo.readCompressed(file);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        if(nbtCompound==null){
            return;
        }

        BlockPos spawnPosition = new BlockPos(nbtCompound.getInt("SpawnX"),nbtCompound.getInt("SpawnY"),nbtCompound.getInt("SpawnZ"));
        NbtList invItemNbts = nbtCompound.getList("Inventory",10);

        YourItemsToNewWorlds.LOGGER.warn("Inventory Items:\n");
        for(NbtElement itemNbt:invItemNbts){
            YourItemsToNewWorlds.LOGGER.warn(((NbtCompound)itemNbt).getString("id"));
        }




    }
}
