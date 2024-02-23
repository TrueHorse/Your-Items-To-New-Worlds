package net.trueHorse.yourItemsToNewWorlds.io;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtIo;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.storage.RegionFile;
import net.minecraft.world.storage.StorageKey;
import org.jetbrains.annotations.Nullable;

import java.io.DataInputStream;
import java.io.IOException;
import java.nio.file.Path;
public class RegionReader {

    private final Path directory;
    private final boolean desync;
    private final StorageKey storageKey;

    public RegionReader(Path worldDirectory, boolean desync){
        this.directory = worldDirectory.resolve("region");
        storageKey = new StorageKey(worldDirectory, RegistryKeys.DIMENSION.)
        this.desync = desync;
    }

    private RegionFile getRegionFile(ChunkPos pos) throws IOException{
        Path path = this.directory.resolve("r." + pos.getRegionX() + "." + pos.getRegionZ() + ".mca");
        return new RegionFile(new StorageKey(),path, this.directory, this.desync);
    }

    @Nullable
    public NbtCompound getNbtAt(ChunkPos pos) throws IOException {
        RegionFile regionFile = this.getRegionFile(pos);
        DataInputStream dataInputStream = regionFile.getChunkInputStream(pos);
        if (dataInputStream == null) {
            return null;
        }

        NbtCompound nbt;
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
