package net.trueHorse.yourItemsToNewWorlds.io;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.trueHorse.yourItemsToNewWorlds.YourItemsToNewWorlds;
import org.apache.commons.lang3.NotImplementedException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.NoSuchElementException;
import java.util.function.Function;

public class ItemImporter {

    public static ArrayList<ItemStack> readItemsFromOtherWorld(int searchLocationDetermMode, BlockPos chosenPos) {
        String path = "C:\\Users\\Paul\\curseforge\\minecraft\\Instances\\All the Mods 8 - ATM8\\saves\\Test World";
        String uuid = "2c143ece-4173-4b31-97ca-bd6c2458fc3a";

        NbtCompound playerNbt = null;

        File file = new File(path + "\\playerdata", uuid + ".dat");
        if (file.exists() && file.isFile()) {
            try {
                playerNbt = NbtIo.readCompressed(file);
            } catch (IOException e) {
                YourItemsToNewWorlds.LOGGER.error("Couldn't read player data file.");
                return new ArrayList<>();
            }
        }

        if (playerNbt == null) {
            return new ArrayList<>();
        }

        NbtList invItemNbts = playerNbt.getList("Inventory", 10);

        YourItemsToNewWorlds.LOGGER.warn("Inventory Items:\n");
        for (NbtElement itemNbt : invItemNbts) {
            YourItemsToNewWorlds.LOGGER.warn(((NbtCompound) itemNbt).getString("id"));
        }

        RegionReader regionReader = new RegionReader(new File(path + "\\region").toPath(), false);
        ChunkPos centerChunkPos;
        try {
            centerChunkPos = switch (searchLocationDetermMode) {
                case 0 -> new ChunkPos(new BlockPos(playerNbt.getInt("SpawnX"), playerNbt.getInt("SpawnY"), playerNbt.getInt("SpawnZ")));
                case 1 -> getContainerChunkPos(regionReader);
                case 2 -> getInhabitationChunkPos(regionReader);
                case 3 -> new ChunkPos(chosenPos);
                default -> throw new NotImplementedException();
            };
        }catch (NoSuchElementException e){
            YourItemsToNewWorlds.LOGGER.error("Failed to process region files.");
            return new ArrayList<>();
        }
        YourItemsToNewWorlds.LOGGER.info("chunkPos: "+centerChunkPos);
        NbtList surroundingChunks = new NbtList();

        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                try {
                    surroundingChunks.add(regionReader.getNbtAt(new ChunkPos(centerChunkPos.x + i, centerChunkPos.z + j)));
                } catch (IOException e) {
                    YourItemsToNewWorlds.LOGGER.error("Couldn't get chunk at relative " + i + " " + j);
                }
            }
        }

        NbtList itemsInBlockEntitiesNbts = new NbtList();
        surroundingChunks.forEach(chunkNbt -> ((NbtCompound) chunkNbt).getList("block_entities", 10).forEach(be -> itemsInBlockEntitiesNbts.addAll(((NbtCompound) be).getList("Items", 10))));

        return new ArrayList<>(itemsInBlockEntitiesNbts.stream().map(nbt -> ItemStack.fromNbt((NbtCompound) nbt)).filter(stack -> !stack.isEmpty()).toList());
    }

    public static ChunkPos getChunkPosWithBiggestSurroundingVal(RegionReader regionReader, Function<ChunkPos,Integer> chunkToValFunc){
        //creating two-dimensional array of values of Chunks
        String[] regionNames = regionReader.getAllRegionFileNames();
        int smallestChunkX = Arrays.stream(regionNames).map(name -> Integer.parseInt(name.split("[.]")[1])).min(Comparator.naturalOrder()).get()*32;
        int biggestChunkX = Arrays.stream(regionNames).map(name -> Integer.parseInt(name.split("[.]")[1])).max(Comparator.naturalOrder()).get()*32+31;
        int smallestChunkY = Arrays.stream(regionNames).map(name -> Integer.parseInt(name.split("[.]")[2])).min(Comparator.naturalOrder()).get()*32;
        int biggestChunkY = Arrays.stream(regionNames).map(name -> Integer.parseInt(name.split("[.]")[2])).max(Comparator.naturalOrder()).get()*32+31;
        int worldWidthInChunks = biggestChunkX-(smallestChunkX-1);
        int worldDepthInChunks = biggestChunkY-(smallestChunkY-1);
        YourItemsToNewWorlds.LOGGER.info("smallX: "+smallestChunkX+"\nsmallY: "+smallestChunkY+"\nbigX: "+biggestChunkX+"\nbigY: "+biggestChunkY+"\nwidth: "+worldWidthInChunks+"\ndepth: "+worldDepthInChunks);
        Integer[][] itemEntitiesInChunks = new Integer[worldWidthInChunks][worldDepthInChunks];

        //getting number for each chunk, summing up 3x3 squares and saving chunk with highest val to return later
        ChunkPos containerChunk = new ChunkPos(0,0);
        int biggestCount = 0;
        for(int i = smallestChunkY+1;i<biggestChunkY;i++){
            for(int j = smallestChunkX+1;j<biggestChunkX;j++){
                int sum = 0;
                for(int k = -1;k<=1;k++){
                    for(int l = -1;l<=1;l++){
                        int chunkIndex1 = j+l-smallestChunkX;
                        int chunkIndex2 = i+k-smallestChunkY;
                        //YourItemsToNewWorlds.LOGGER.info("i j k l"+i+" "+j+" "+k+" "+l);
                        //YourItemsToNewWorlds.LOGGER.info("Indices: "+chunkIndex1+" "+chunkIndex2);
                        if(itemEntitiesInChunks[chunkIndex1][chunkIndex2]==null){
                            itemEntitiesInChunks[chunkIndex1][chunkIndex2] = chunkToValFunc.apply(new ChunkPos(j+l,i+k));
                        }
                        sum += itemEntitiesInChunks[chunkIndex1][chunkIndex2];
                    }
                }
                if(sum>biggestCount){
                    biggestCount = sum;
                    containerChunk = new ChunkPos(j,i);
                }
            }
        }
        //return chunk with the biggest surrounding value
        return containerChunk;
    }

    public static ChunkPos getContainerChunkPos(RegionReader regionReader) throws NoSuchElementException{
        return getChunkPosWithBiggestSurroundingVal(regionReader,chunkPos -> {
            try {
                return (int)regionReader.getNbtAt(chunkPos).getList("block_entities", 10).stream().filter(nbt -> !((NbtCompound)nbt).getList("Items",10).isEmpty()).count();
            } catch (IOException e) {
                YourItemsToNewWorlds.LOGGER.error("Couldn't read region file "+(Math.floor(chunkPos.x/32.0))+"."+(Math.floor(chunkPos.z/32.0)));
                return 0;
            } catch(NullPointerException e){
                return 0;
            }
        });
    }

    public static ChunkPos getInhabitationChunkPos(RegionReader regionReader){
        throw new NotImplementedException();
    }

}
