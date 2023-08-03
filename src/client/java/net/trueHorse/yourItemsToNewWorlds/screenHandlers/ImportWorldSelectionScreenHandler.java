package net.trueHorse.yourItemsToNewWorlds.screenHandlers;

import net.minecraft.client.MinecraftClient;
import net.minecraft.world.level.storage.LevelSummary;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class ImportWorldSelectionScreenHandler {

    private List<LevelSummary> worlds;
    private final List<Path> instancePaths;
    private Path selectedInstancePath;

    public ImportWorldSelectionScreenHandler(){
        instancePaths = new ArrayList<>(List.of(MinecraftClient.getInstance().runDirectory.toPath()));
    }

    public Path getPathOfWorld(int index){
        return selectedInstancePath.resolve("saves/"+worlds.get(index).getName());
    }

    public void addInstance(Path instance){
        instancePaths.add(instance);
    }

    public List<Path> getInstances(){
        return instancePaths;
    }

    public Path getSelectedInstancePath(){
        return selectedInstancePath;
    }

    public void setSelectedInstancePath(Path path){
        this.selectedInstancePath = path;
    }

    public List<LevelSummary> getWorlds() {
        return worlds;
    }

    public void setWorlds(List<LevelSummary> worlds) {
        this.worlds = worlds;
    }
}
