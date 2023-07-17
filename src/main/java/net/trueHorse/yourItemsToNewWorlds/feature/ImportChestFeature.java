package net.trueHorse.yourItemsToNewWorlds.feature;

import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.LootableContainerBlockEntity;
import net.minecraft.loot.LootTables;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.Heightmap;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.feature.DefaultFeatureConfig;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.util.FeatureContext;

import java.util.stream.IntStream;

public class ImportChestFeature extends Feature<DefaultFeatureConfig> {
    public ImportChestFeature(Codec<DefaultFeatureConfig> configCodec) {
        super(configCodec);
    }

    @Override
    public boolean generate(FeatureContext<DefaultFeatureConfig> context) {
        Random random = context.getRandom();
        StructureWorldAccess structureWorldAccess = context.getWorld();
        ChunkPos chunkPos = new ChunkPos(context.getOrigin());
        IntArrayList intArrayList = Util.shuffle(IntStream.rangeClosed(chunkPos.getStartX(), chunkPos.getEndX()), random);
        IntArrayList intArrayList2 = Util.shuffle(IntStream.rangeClosed(chunkPos.getStartZ(), chunkPos.getEndZ()), random);
        BlockPos.Mutable mutable = new BlockPos.Mutable();
        for (Integer integer : intArrayList) {
            for (Integer integer2 : intArrayList2) {
                mutable.set(integer, 0, integer2);
                BlockPos blockPos = structureWorldAccess.getTopPosition(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, mutable);
                if (!structureWorldAccess.isAir(blockPos) && !structureWorldAccess.getBlockState(blockPos).getCollisionShape(structureWorldAccess, blockPos).isEmpty()) continue;
                structureWorldAccess.setBlockState(blockPos, Blocks.CHEST.getDefaultState(), Block.NOTIFY_LISTENERS);
                LootableContainerBlockEntity.setLootTable(structureWorldAccess, random, blockPos, LootTables.SPAWN_BONUS_CHEST);
                BlockState blockState = Blocks.DIAMOND_BLOCK.getDefaultState();
                for (Direction direction : Direction.Type.HORIZONTAL) {
                    BlockPos blockPos2 = blockPos.offset(direction);
                    if (!blockState.canPlaceAt(structureWorldAccess, blockPos2)) continue;
                    structureWorldAccess.setBlockState(blockPos2, blockState, Block.NOTIFY_LISTENERS);
                }
                return true;
            }
        }
        return false;
    }
}
