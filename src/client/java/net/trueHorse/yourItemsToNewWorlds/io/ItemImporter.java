package net.trueHorse.yourItemsToNewWorlds.io;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
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

    public static ArrayList<ItemStack> readItemsFromOtherWorld(String worldPath,String playerUuid,int searchLocationDetermMode){
        return readItemsFromOtherWorld(worldPath,playerUuid,searchLocationDetermMode,null);
    }

    public static ArrayList<ItemStack> readItemsFromOtherWorld(String worldPath,String playerUuid, int searchLocationDetermMode, BlockPos chosenPos) {
        ArrayList<ItemStack> items = new ArrayList<>();

        NbtCompound playerNbt = null;

        File file = new File(worldPath + "\\playerdata", playerUuid + ".dat");
        if (file.exists() && file.isFile()) {
            try {
                playerNbt = NbtIo.readCompressed(file);
            } catch (IOException e) {
                YourItemsToNewWorlds.LOGGER.error("Couldn't read player data file.\n"+e.getMessage());
                return new ArrayList<>();
            }
        }

        if (playerNbt == null) {
            return new ArrayList<>();
        }

        items.addAll(playerNbt.getList("Inventory", 10).stream().map(nbt -> ItemStack.fromNbt((NbtCompound)nbt)).filter(stack -> !stack.isEmpty()).toList());
        items.addAll(playerNbt.getList("EnderItems", 10).stream().map(nbt -> ItemStack.fromNbt((NbtCompound)nbt)).filter(stack -> !stack.isEmpty()).toList());

        RegionReader regionReader = new RegionReader(new File(worldPath + "\\region").toPath(), false);
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
                    YourItemsToNewWorlds.LOGGER.error("Couldn't get chunk at " + (i+centerChunkPos.x) + " " + (j+ centerChunkPos.z));
                }
            }
        }

        NbtList itemsInBlockEntitiesNbts = new NbtList();
        surroundingChunks.forEach(chunkNbt -> ((NbtCompound) chunkNbt).getList("block_entities", 10).forEach(be -> itemsInBlockEntitiesNbts.addAll(((NbtCompound) be).getList("Items", 10))));
        surroundingChunks.forEach(chunkNbt -> ((NbtCompound) chunkNbt).getCompound("Level").getList("TileEntities", 10).forEach(be -> itemsInBlockEntitiesNbts.addAll(((NbtCompound) be).getList("Items", 10))));

        items.addAll(itemsInBlockEntitiesNbts.stream().map(nbt -> ItemStack.fromNbt((NbtCompound) nbt)).filter(stack -> !stack.isEmpty()).toList());
        return items;
    }

    private static ChunkPos getChunkPosWithBiggestSurroundingVal(RegionReader regionReader, Function<ChunkPos,Integer> chunkToValFunc){
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
        long biggestVal = 0;
        for(int i = smallestChunkY+1;i<biggestChunkY;i++){
            for(int j = smallestChunkX+1;j<biggestChunkX;j++){
                long sum = 0;
                for(int k = -1;k<=1;k++){
                    for(int l = -1;l<=1;l++){
                        int chunkIndex1 = j+l-smallestChunkX;
                        int chunkIndex2 = i+k-smallestChunkY;
                        //YourItemsToNewWorlds.LOGGER.info("i j k l"+i+" "+j+" "+k+" "+l);
                        //YourItemsToNewWorlds.LOGGER.info("Indices: "+chunkIndex1+" "+chunkIndex2);
                        if(itemEntitiesInChunks[chunkIndex1][chunkIndex2]==null){
                            itemEntitiesInChunks[chunkIndex1][chunkIndex2] = chunkToValFunc.apply(new ChunkPos(j+l,i+k));
                        }
                        try{
                            sum = Math.addExact(sum,itemEntitiesInChunks[chunkIndex1][chunkIndex2]);
                        }catch (ArithmeticException e){
                            sum = Long.MAX_VALUE;
                        }
                    }
                }
                if(sum>biggestVal){
                    biggestVal = sum;
                    containerChunk = new ChunkPos(j,i);
                    if(biggestVal == Long.MAX_VALUE){
                        return containerChunk;
                    }
                }
            }
        }
        //return chunk with the biggest surrounding value
        return containerChunk;
    }

    private static ChunkPos getContainerChunkPos(RegionReader regionReader) throws NoSuchElementException{
        return getChunkPosWithBiggestSurroundingVal(regionReader,chunkPos -> {
            try {
                NbtCompound chunkNbt = regionReader.getNbtAt(chunkPos);
                return (int)chunkNbt.getList("block_entities", 10).stream().filter(nbt -> !((NbtCompound)nbt).getList("Items",10).isEmpty()).count()
                        + (int)chunkNbt.getCompound("Level").getList("TileEntities", 10).stream().filter(nbt -> !((NbtCompound)nbt).getList("Items",10).isEmpty()).count();
            } catch (IOException e) {
                YourItemsToNewWorlds.LOGGER.error("Couldn't read region file "+(Math.floor(chunkPos.x/32.0))+"."+(Math.floor(chunkPos.z/32.0)));
                return 0;
            } catch(NullPointerException e){
                return 0;
            }
        });
    }

    private static ChunkPos getInhabitationChunkPos(RegionReader regionReader){
        return getChunkPosWithBiggestSurroundingVal(regionReader,chunkPos -> {
            try {
                NbtCompound chunkNbt = regionReader.getNbtAt(chunkPos);
                return Math.toIntExact(chunkNbt.getLong("InhabitedTime"))
                        + Math.toIntExact(chunkNbt.getCompound("Level").getLong("InhabitedTime"));
            } catch (IOException e) {
                YourItemsToNewWorlds.LOGGER.error("Couldn't read region file "+(Math.floor(chunkPos.x/32.0))+"."+(Math.floor(chunkPos.z/32.0)));
                return 0;
            } catch(NullPointerException e){
                return 0;
            }catch (ArithmeticException e){
                return Integer.MAX_VALUE;
            }
        });
    }

}
