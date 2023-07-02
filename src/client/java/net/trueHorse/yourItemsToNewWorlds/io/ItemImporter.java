package net.trueHorse.yourItemsToNewWorlds.io;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.trueHorse.yourItemsToNewWorlds.YourItemsToNewWorlds;
import net.trueHorse.yourItemsToNewWorlds.storage.RegionReader;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class ItemImporter {

    public static ArrayList<ItemStack> readItemsFromOtherWorld() {
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
            return new ArrayList<>();
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

        return new ArrayList<>(itemsInBlockEntitiesNbts.stream().map(nbt -> ItemStack.fromNbt((NbtCompound) nbt)).filter(stack -> !stack.isEmpty()).toList());
    }

}
