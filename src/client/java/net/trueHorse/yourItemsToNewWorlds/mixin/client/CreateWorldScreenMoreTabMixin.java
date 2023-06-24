package net.trueHorse.yourItemsToNewWorlds.mixin.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.world.CreateWorldScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.GridWidget;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtList;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.trueHorse.yourItemsToNewWorlds.YourItemsToNewWorlds;
import net.trueHorse.yourItemsToNewWorlds.gui.ImportItemsScreen;
import net.trueHorse.yourItemsToNewWorlds.storage.RegionReader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.io.File;
import java.io.IOException;

@Mixin(targets = "net/minecraft/client/gui/screen/world/CreateWorldScreen$MoreTab")
public class CreateWorldScreenMoreTabMixin {

    @Shadow
    private CreateWorldScreen field_42178;

    @Inject(method = "<init>", at = @At("TAIL"), locals = LocalCapture.CAPTURE_FAILSOFT)
    private void addItemTransferButton(CreateWorldScreen createWorldScreen, CallbackInfo ci, GridWidget.Adder adder) {
        adder.add(ButtonWidget.builder(Text.of("Transfertest"), button -> MinecraftClient.getInstance().setScreen(new ImportItemsScreen(field_42178, list -> YourItemsToNewWorlds.LOGGER.warn("apply")))).width(210).build());
    }

    private void printItemsFromOtherWorld() {
        String path = "C:\\Users\\Paul\\curseforge\\minecraft\\Instances\\All the Mods 8 - ATM8\\saves\\Test World";
        String uuid = "2c143ece-4173-4b31-97ca-bd6c2458fc3a";

        NbtCompound nbtCompound = null;

        File file = new File(path + "\\playerdata", uuid + ".dat");
        if (file.exists() && file.isFile()) {
            try {
                nbtCompound = NbtIo.readCompressed(file);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        if (nbtCompound == null) {
            return;
        }

        BlockPos spawnPosition = new BlockPos(nbtCompound.getInt("SpawnX"), nbtCompound.getInt("SpawnY"), nbtCompound.getInt("SpawnZ"));
        NbtList invItemNbts = nbtCompound.getList("Inventory", 10);

        YourItemsToNewWorlds.LOGGER.warn(spawnPosition.toString());

        YourItemsToNewWorlds.LOGGER.warn("Inventory Items:\n");
        for (NbtElement itemNbt : invItemNbts) {
            YourItemsToNewWorlds.LOGGER.warn(((NbtCompound) itemNbt).getString("id"));
        }

        int spawnChunkX = ChunkSectionPos.getSectionCoord(spawnPosition.getX());
        int spawnChunkY = ChunkSectionPos.getSectionCoord(spawnPosition.getY());
        NbtList surroundingChunks = new NbtList();
        RegionReader regionReader = new RegionReader(new File(path + "\\region").toPath(), false);

        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                try {
                    surroundingChunks.add(regionReader.getNbtAt(new ChunkPos(spawnChunkX + i, spawnChunkY + j)));
                } catch (IOException e) {
                    YourItemsToNewWorlds.LOGGER.error("Couldn't get chunk at relative " + i + " " + j);
                }
            }
        }

        NbtList itemsInBlockEntitiesNbts = new NbtList();
        surroundingChunks.forEach(chunkNbt -> ((NbtCompound) chunkNbt).getList("block_entities", 10).forEach(be -> itemsInBlockEntitiesNbts.addAll(((NbtCompound) be).getList("Items", 10))));
        surroundingChunks.forEach(chunkNbt -> ((NbtCompound) chunkNbt).getList("block_entities", 10).forEach(be -> YourItemsToNewWorlds.LOGGER.warn(be.toString())));
        YourItemsToNewWorlds.LOGGER.warn("Block Entity Items:\n");
        itemsInBlockEntitiesNbts.forEach(nbt -> YourItemsToNewWorlds.LOGGER.warn(((NbtCompound) nbt).getString("id")));
    }

}
