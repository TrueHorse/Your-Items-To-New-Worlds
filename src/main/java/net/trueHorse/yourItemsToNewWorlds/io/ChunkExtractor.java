package net.trueHorse.yourItemsToNewWorlds.io;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.trueHorse.yourItemsToNewWorlds.YourItemsToNewWorlds;

public class ChunkExtractor {

    public static ListTag extractItems(ListTag chunkNbts){
        ListTag blockEntityNbts = extractBlockEntities(chunkNbts);

        return getItemsFromEntities(blockEntityNbts);
    }

    public static ListTag extractBlockEntities(ListTag chunkNbts){
        if(chunkNbts.isEmpty()){
            return new ListTag();
        }else{
            ListTag blockEntityNbts = new ListTag();

            if(((CompoundTag)chunkNbts.get(0)).contains("block_entities")||((CompoundTag)chunkNbts.get(0)).contains("Level")){
                if(((CompoundTag)chunkNbts.get(0)).contains("block_entities")){
                    chunkNbts.forEach(chunkNbt->blockEntityNbts.addAll(((CompoundTag) chunkNbt).getList("block_entities", 10)));
                    YourItemsToNewWorlds.LOGGER.info("Block Entities in chunks: "+ blockEntityNbts.size());
                }else{
                    chunkNbts.forEach(chunkNbt->blockEntityNbts.addAll(((CompoundTag) chunkNbt).getCompound("Level").getList("TileEntities", 10)));
                    YourItemsToNewWorlds.LOGGER.info("Tile Entities in chunks: "+ blockEntityNbts.size());
                }
            }else{
                YourItemsToNewWorlds.LOGGER.warn("Unknown chunk format.");
            }

            return blockEntityNbts;
        }
    }

    private static ListTag getItemsFromEntities(ListTag blockEntitieNbts){
        ListTag itemNbts = new ListTag();

        for(Tag blockEntity : blockEntitieNbts){
            CompoundTag blockEntityC = ((CompoundTag)blockEntity);
            itemNbts.addAll(BlockEntityStrategies.getStrategy(blockEntityC).apply(blockEntityC));
        }

        return itemNbts;
    }

}
