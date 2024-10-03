package net.trueHorse.yourItemsToNewWorlds.io;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.trueHorse.yourItemsToNewWorlds.YourItemsToNewWorlds;

public class ChunkExtractor {

    public static NbtList extractItems(NbtList chunkNbts){
        NbtList blockEntityNbts = extractBlockEntities(chunkNbts);

        return getItemsFromEntities(blockEntityNbts);
    }

    public static NbtList extractBlockEntities(NbtList chunkNbts){
        if(chunkNbts.isEmpty()){
            return new NbtList();
        }else{
            NbtList blockEntityNbts = new NbtList();

            if(((NbtCompound)chunkNbts.get(0)).contains("block_entities")||((NbtCompound)chunkNbts.get(0)).contains("Level")){
                if(((NbtCompound)chunkNbts.get(0)).contains("block_entities")){
                    chunkNbts.forEach(chunkNbt->blockEntityNbts.addAll(((NbtCompound) chunkNbt).getList("block_entities", 10)));
                    YourItemsToNewWorlds.LOGGER.info("Block Entities in chunks: "+ blockEntityNbts.size());
                }else{
                    chunkNbts.forEach(chunkNbt->blockEntityNbts.addAll(((NbtCompound) chunkNbt).getCompound("Level").getList("TileEntities", 10)));
                    YourItemsToNewWorlds.LOGGER.info("Tile Entities in chunks: "+ blockEntityNbts.size());
                }
            }else{
                YourItemsToNewWorlds.LOGGER.warn("Unknown chunk format.");
            }

            return blockEntityNbts;
        }
    }

    private static NbtList getItemsFromEntities(NbtList blockEntitieNbts){
        NbtList itemNbts = new NbtList();

        for(NbtElement blockEntity : blockEntitieNbts){
            NbtCompound blockEntityC = ((NbtCompound)blockEntity);
            itemNbts.addAll(BlockEntityStrategies.getStrategy(blockEntityC).apply(blockEntityC));
        }

        return itemNbts;
    }

}
