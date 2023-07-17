package net.trueHorse.yourItemsToNewWorlds.feature;

import com.mojang.serialization.Codec;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Heightmap;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.feature.DefaultFeatureConfig;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.util.FeatureContext;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class ImportChestsFeature extends Feature<DefaultFeatureConfig> {

    public static ArrayList<ItemStack> importItems = new ArrayList<>();
    public ImportChestsFeature(Codec<DefaultFeatureConfig> configCodec) {
        super(configCodec);
    }

    @Override
    public boolean generate(FeatureContext<DefaultFeatureConfig> context) {
        StructureWorldAccess structureWorldAccess = context.getWorld();
        BlockPos origin = context.getOrigin();
        int chestCount = (int)Math.ceil(importItems.size()/27.0);

        BlockPos.Mutable mutable = origin.mutableCopy();
        mutable.setZ(mutable.getZ()+2);
        while (true) {
            mutable.setZ(mutable.getZ() + 1);
            ArrayList<BlockPos> blockPositions = this.getGenerationPositions(mutable,chestCount,structureWorldAccess);
            if(blockPositions.stream().map(blockPos -> !structureWorldAccess.isAir(blockPos) && !structureWorldAccess.getBlockState(blockPos).getCollisionShape(structureWorldAccess, blockPos).isEmpty()).noneMatch(Predicate.isEqual(true))){
                for (int i = 0;i<blockPositions.size();i++) {
                    BlockState blockState = Blocks.CHEST.getDefaultState();
                    ChestBlockEntity blockEntity = (ChestBlockEntity) ((ChestBlock) Blocks.CHEST).createBlockEntity(blockPositions.get(i), blockState);
                    List<ItemStack> chestContent = importItems.subList(i*27,importItems.size()-i*27<27?importItems.size():(i+1)*27);
                    for(int j = 0;j<chestContent.size();j++){
                        blockEntity.setStack(j,chestContent.get(j));
                    }

                    structureWorldAccess.setBlockState(blockPositions.get(i), blockState, Block.NOTIFY_LISTENERS);
                    structureWorldAccess.getChunk(blockPositions.get(i)).setBlockEntity(blockEntity);
                }
                return true;
            }
        }
    }

    public ArrayList<BlockPos> getGenerationPositions(BlockPos pos, int chestCount, StructureWorldAccess structureWorldAccess){
        ArrayList<BlockPos> blockPositions = new ArrayList<>();
        BlockPos blockPos = structureWorldAccess.getTopPosition(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, pos);
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
