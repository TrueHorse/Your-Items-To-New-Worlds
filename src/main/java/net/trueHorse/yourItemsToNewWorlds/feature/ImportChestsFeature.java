package net.trueHorse.yourItemsToNewWorlds.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class ImportChestsFeature extends Feature<NoneFeatureConfiguration> {

    public static ArrayList<ItemStack> importItems = new ArrayList<>();
    public ImportChestsFeature(Codec<NoneFeatureConfiguration> configCodec) {
        super(configCodec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel structureWorldAccess = context.level();
        BlockPos origin = context.origin();
        int chestCount = (int)Math.ceil(importItems.size()/27.0);

        BlockPos.MutableBlockPos mutable = origin.mutable();
        mutable.setZ(mutable.getZ()+2);
        while (true) {
            mutable.setZ(mutable.getZ() + 1);
            ArrayList<BlockPos> blockPositions = this.getGenerationPositions(mutable,chestCount,structureWorldAccess);
            if(blockPositions.stream().map(blockPos -> !structureWorldAccess.isEmptyBlock(blockPos) && !structureWorldAccess.getBlockState(blockPos).getCollisionShape(structureWorldAccess, blockPos).isEmpty()).noneMatch(Predicate.isEqual(true))){
                for (int i = 0;i<blockPositions.size();i++) {
                    BlockState blockState = Blocks.CHEST.defaultBlockState();
                    ChestBlockEntity blockEntity = (ChestBlockEntity) ((ChestBlock) Blocks.CHEST).newBlockEntity(blockPositions.get(i), blockState);
                    List<ItemStack> chestContent = importItems.subList(i*27,importItems.size()-i*27<27?importItems.size():(i+1)*27);
                    for(int j = 0;j<chestContent.size();j++){
                        blockEntity.setItem(j,chestContent.get(j));
                    }

                    structureWorldAccess.setBlock(blockPositions.get(i), blockState, Block.UPDATE_CLIENTS);
                    structureWorldAccess.getChunk(blockPositions.get(i)).setBlockEntity(blockEntity);
                }
                return true;
            }
        }
    }

    public ArrayList<BlockPos> getGenerationPositions(BlockPos pos, int chestCount, WorldGenLevel structureWorldAccess){
        ArrayList<BlockPos> blockPositions = new ArrayList<>();
        BlockPos blockPos = structureWorldAccess.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, pos);
        int chestStackHeight = (int) Math.ceil(chestCount / 2.0);
        int addedPositions = 0;
        for (int i = 0; i < chestStackHeight; i++) {
            for (int j = 0; j < 2; j++) {
                blockPositions.add(new BlockPos(blockPos.getX()-j, blockPos.getY() + i, blockPos.getZ()));
                addedPositions++;
                if(addedPositions==chestCount){
                    break;
                }
            }
        }
        return blockPositions;
    }
}
