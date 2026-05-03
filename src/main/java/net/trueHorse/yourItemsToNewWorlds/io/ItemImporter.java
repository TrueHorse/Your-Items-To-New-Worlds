package net.trueHorse.yourItemsToNewWorlds.io;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.trueHorse.yourItemsToNewWorlds.YourItemsToNewWorlds;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Function;

public class ItemImporter {

    public enum SearchLocationDeterminationMode {
        SPAWN_POINT,
        MOST_ITEM_CONTAINERS,
        LONGEST_INHABITATION,
        COORDINATES
    }
    private final RegionReader regionReader;
    private final CompoundTag playerNbt;
    private ChunkPos searchedChunkPos;
    private Map<ChunkPos,CompoundTag> searchedChunks = Map.of();
    private final Map<String, List<CompoundTag>> originalStackNbts = new HashMap<>();
    private final File playerFile;
    private final Path worldPath;

    public ItemImporter(Path worldPath, String playerUuid){
        this.worldPath = worldPath;
        regionReader = new RegionReader(worldPath.resolve("region"), false);

        CompoundTag tempPlayerNbt;
        playerFile = worldPath.resolve("playerdata/"+ playerUuid + ".dat").toFile();
        if (playerFile.exists() && playerFile.isFile()) {
            try {
                tempPlayerNbt = NbtIo.readCompressed(playerFile);
            } catch (IOException e) {
                YourItemsToNewWorlds.LOGGER.error("Couldn't read player data file.\n"+e.getMessage());
                tempPlayerNbt = null;
            }
        }else{
            YourItemsToNewWorlds.LOGGER.error(playerFile.getName()+" doesn't exist.");
            tempPlayerNbt = null;
        }
        playerNbt = tempPlayerNbt;
    }

    public ArrayList<ItemStack> getPlayerItems(){
        if (playerNbt == null) {
            return new ArrayList<>();
        }

        ArrayList<Tag> itemNbts = new ArrayList<>();
        itemNbts.addAll(playerNbt.getList("Inventory", 10));
        itemNbts.addAll(playerNbt.getList("EnderItems", 10));

        itemNbts.forEach((entityStackNbt)->{
            CompoundTag stackNbt = new CompoundTag();
            ItemStack.of((CompoundTag) entityStackNbt).save(stackNbt);

            originalStackNbts.compute(stackNbt.toString(),
                    (keyStack,valueList)-> {
                        if(valueList==null){
                            valueList = new ArrayList<>();
                        }
                        valueList.add((CompoundTag) entityStackNbt);
                        return valueList;
                    });
        });

        return new ArrayList<>(itemNbts.stream().map(nbt -> ItemStack.of((CompoundTag)nbt)).filter(stack -> !stack.isEmpty()).toList());
    }

    public ArrayList<ItemStack> getItemsInArea(ChunkPos centerChunkPos, int searchRadius) {
        searchedChunkPos = centerChunkPos;
        searchedChunks = new HashMap<>();

        for (int i = searchRadius*-1; i <= searchRadius; i++) {
            for (int j = searchRadius*-1; j <= searchRadius; j++) {
                try {
                    ChunkPos chunkPos = new ChunkPos(centerChunkPos.x + i, centerChunkPos.z + j);
                    searchedChunks.put(chunkPos,regionReader.getNbtAt(chunkPos));
                } catch (IOException | NullPointerException e) {
                    YourItemsToNewWorlds.LOGGER.error("Couldn't get chunk at " + (i+centerChunkPos.x) + " " + (j+ centerChunkPos.z));
                }
            }
        }

        YourItemsToNewWorlds.LOGGER.info("Found chunks: "+ searchedChunks.size());
        ListTag itemsInBlockEntitiesNbts = ChunkExtractor.extractItems(searchedChunks.values().stream().toList());
        /*
        itemsInBlockEntitiesNbts.forEach(nbt -> {
            ((CompoundTag)nbt).getKeys().forEach(key->{
                Tag el = ((CompoundTag) nbt).get(key);
                YourItemsToNewWorlds.LOGGER.info(key+" "+el.toString()+": "+el.getNbtType().getCommandFeedbackName());
            });
        });
         */

        itemsInBlockEntitiesNbts.forEach((entityStackNbt)->{
            CompoundTag stackNbt = new CompoundTag();
            ItemStack.of((CompoundTag) entityStackNbt).save(stackNbt);

            originalStackNbts.compute(stackNbt.toString(),
                    (keyStack,valueList)-> {
                        if(valueList==null){
                            valueList = new ArrayList<>();
                        }
                        valueList.add((CompoundTag) entityStackNbt);
                        return valueList;
                    });
        });

        return new ArrayList<>(itemsInBlockEntitiesNbts.stream().map(nbt -> ItemStack.of((CompoundTag) nbt)).filter(stack -> !stack.isEmpty()).toList());
    }

    private ChunkPos getChunkPosWithBiggestSurroundingVal(RegionReader regionReader, int searchRadius, Function<ChunkPos,Integer> chunkToValFunc){
        //creating two-dimensional array of values of Chunks
        String[] regionNames = regionReader.getAllRegionFileNames();
        int smallestChunkX = Arrays.stream(regionNames).map(name -> Integer.parseInt(name.split("[.]")[1])).min(Comparator.naturalOrder()).get()*32;
        int biggestChunkX = Arrays.stream(regionNames).map(name -> Integer.parseInt(name.split("[.]")[1])).max(Comparator.naturalOrder()).get()*32+31;
        int smallestChunkY = Arrays.stream(regionNames).map(name -> Integer.parseInt(name.split("[.]")[2])).min(Comparator.naturalOrder()).get()*32;
        int biggestChunkY = Arrays.stream(regionNames).map(name -> Integer.parseInt(name.split("[.]")[2])).max(Comparator.naturalOrder()).get()*32+31;
        int worldWidthInChunks = biggestChunkX-(smallestChunkX-1);
        int worldDepthInChunks = biggestChunkY-(smallestChunkY-1);
        //YourItemsToNewWorlds.LOGGER.info("smallX: "+smallestChunkX+"\nsmallY: "+smallestChunkY+"\nbigX: "+biggestChunkX+"\nbigY: "+biggestChunkY+"\nwidth: "+worldWidthInChunks+"\ndepth: "+worldDepthInChunks);
        Integer[][] itemEntitiesInChunks = new Integer[worldWidthInChunks][worldDepthInChunks];

        //getting number for each chunk, summing up 3x3 squares and saving chunk with highest val to return later
        ChunkPos containerChunk = new ChunkPos(0,0);
        long biggestVal = 0;
        for(int i = smallestChunkY+1;i<biggestChunkY;i++){
            for(int j = smallestChunkX+1;j<biggestChunkX;j++){
                long sum = 0;
                for(int k = searchRadius*-1;k<=searchRadius;k++){
                    for(int l = searchRadius*-1;l<=searchRadius;l++){
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

    public ChunkPos getSearchChunkPos(SearchLocationDeterminationMode searchLocationDetermMode, int searchRadius, BlockPos chosenPos){
        ChunkPos centerChunkPos;
        try {
            centerChunkPos = switch (searchLocationDetermMode) {
                case SPAWN_POINT -> new ChunkPos(new BlockPos(playerNbt.getInt("SpawnX"), playerNbt.getInt("SpawnY"), playerNbt.getInt("SpawnZ")));
                case MOST_ITEM_CONTAINERS-> getContainerChunkPos(regionReader,searchRadius);
                case LONGEST_INHABITATION -> getInhabitationChunkPos(regionReader,searchRadius);
                case COORDINATES -> new ChunkPos(chosenPos);
            };
        }catch (NoSuchElementException e){
            YourItemsToNewWorlds.LOGGER.error("Failed to process region files.");
            centerChunkPos = null;
        }
        return  centerChunkPos;
    }

    private ChunkPos getContainerChunkPos(RegionReader regionReader,int searchRadius) throws NoSuchElementException{
        return getChunkPosWithBiggestSurroundingVal(regionReader,searchRadius,chunkPos -> {
            try {
                CompoundTag chunkNbt = regionReader.getNbtAt(chunkPos);
                return (int)chunkNbt.getList("block_entities", 10).stream().filter(nbt -> !((CompoundTag)nbt).getList("Items",10).isEmpty()).count()
                        + (int)chunkNbt.getCompound("Level").getList("TileEntities", 10).stream().filter(nbt -> !((CompoundTag)nbt).getList("Items",10).isEmpty()).count();
            } catch (IOException e) {
                YourItemsToNewWorlds.LOGGER.error("Couldn't read region file "+(Math.floor(chunkPos.x/32.0))+"."+(Math.floor(chunkPos.z/32.0)));
                return 0;
            } catch(NullPointerException e){
                return 0;
            }
        });
    }

    private ChunkPos getInhabitationChunkPos(RegionReader regionReader, int searchRadius){
        return getChunkPosWithBiggestSurroundingVal(regionReader,searchRadius,chunkPos -> {
            try {
                CompoundTag chunkNbt = regionReader.getNbtAt(chunkPos);
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

    public void deleteItemsInWorld(List<ItemStack> selectedItemStacks) throws IOException{
        File worldFile = worldPath.resolve("level.dat").toFile();
        CompoundTag worldNbt = NbtIo.readCompressed(worldFile);
        CompoundTag playerNbt2 = worldNbt.getCompound("Data").getCompound("Player");

        //TODO refactor to not miss any storage location (Inventory, Ender Chest...)
        selectedItemStacks.forEach((itemStack)->{
            CompoundTag stackNbt = new CompoundTag();
            itemStack.save(stackNbt);

            List<CompoundTag> possibleOriginalNbts = originalStackNbts.get(stackNbt.toString());

            for(CompoundTag possibleNbt : possibleOriginalNbts){
                if (playerNbt.getList("Inventory", 10).contains(possibleNbt)) {
                    playerNbt.getList("Inventory", 10).remove(possibleNbt);
                    playerNbt2.getList("Inventory", 10).remove(possibleNbt);
                    return;
                } else if (playerNbt.getList("EnderItems", 10).contains(possibleNbt)) {
                    playerNbt.getList("EnderItems", 10).remove(possibleNbt);
                    playerNbt2.getList("EnderItems", 10).remove(possibleNbt);
                    return;
                } else {
                    ListTag blockEntities = ChunkExtractor.extractBlockEntities(searchedChunks.values().stream().toList());
                    for (Tag entityNbt : blockEntities) {
                        //TODO use strategies for other entities

                        if (((CompoundTag) entityNbt).getList("Items", 10).remove(possibleNbt)) {
                            return;
                        }
                    }


                }
            }
            YourItemsToNewWorlds.LOGGER.error("{} could not be removed from the selected World.", itemStack.getDisplayName().getString());
        });

        searchedChunks.forEach((chunkPos,nbt)->{
            try {
                regionReader.write(chunkPos,nbt);
            } catch (IOException e) {
                YourItemsToNewWorlds.LOGGER.error("Couldn't delete items in region file "+(Math.floor(chunkPos.x/32.0))+"."+(Math.floor(chunkPos.z/32.0)));
            }
        });
        NbtIo.writeCompressed(playerNbt,playerFile);
        NbtIo.writeCompressed(worldNbt,worldFile);
    }

    public ChunkPos getSearchedChunkPos() {
        return searchedChunkPos;
    }

}
