package net.trueHorse.yourItemsToNewWorlds.io;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.storage.RegionFile;
import org.jetbrains.annotations.Nullable;

import java.io.DataInputStream;
import java.io.IOException;
import java.nio.file.Path;
public class RegionReader {

    private final Path directory;
    private final boolean desync;

    public RegionReader(Path directory, boolean desync){
        this.directory = directory;
        this.desync = desync;
    }

    private RegionFile getRegionFile(ChunkPos pos) throws IOException{
        Path path = this.directory.resolve("r." + pos.getRegionX() + "." + pos.getRegionZ() + ".mca");
        return new RegionFile(path, this.directory, this.desync);
    }

    @Nullable
    public CompoundTag getNbtAt(ChunkPos pos) throws IOException {
        RegionFile regionFile = this.getRegionFile(pos);
        DataInputStream dataInputStream = regionFile.getChunkDataInputStream(pos);
        if (dataInputStream == null) {
            return null;
        }

        CompoundTag nbt;
        try {
            nbt = NbtIo.read(dataInputStream);
        } catch (Throwable var7) {
            try {
                dataInputStream.close();
            } catch (Throwable var6) {
                var7.addSuppressed(var6);
            }

            throw var7;
        }

        dataInputStream.close();

        return nbt;
    }

    public String[] getAllRegionFileNames(){
        return directory.toFile().list();
    }
}
