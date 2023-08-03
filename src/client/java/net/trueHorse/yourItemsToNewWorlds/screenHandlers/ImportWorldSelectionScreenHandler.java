package net.trueHorse.yourItemsToNewWorlds.screenHandlers;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.FatalErrorScreen;
import net.minecraft.text.Text;
import net.minecraft.world.level.storage.LevelStorage;
import net.minecraft.world.level.storage.LevelStorageException;
import net.minecraft.world.level.storage.LevelSummary;
import net.trueHorse.yourItemsToNewWorlds.YourItemsToNewWorlds;
import net.trueHorse.yourItemsToNewWorlds.gui.ImportWorldSelectionScreen;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class ImportWorldSelectionScreenHandler {

    private final ImportWorldSelectionScreen screen;
    private List<LevelSummary> worlds = new ArrayList<>();
    private final List<Path> instancePaths;
    private Path selectedInstancePath = null;

    public ImportWorldSelectionScreenHandler(ImportWorldSelectionScreen screen){
        this.screen = screen;
        instancePaths = new ArrayList<>(List.of(MinecraftClient.getInstance().runDirectory.toPath()));
    }

    public void onInstanceSelected(Path path){
        selectedInstancePath = path;
        LevelStorage levelStorage = new LevelStorage(path.resolve("saves"),path.resolve("backups"),LevelStorage.createSymlinkFinder(path.resolve("allowed_symlinks.txt")),MinecraftClient.getInstance().getDataFixer());
        try {
            worlds = levelStorage.loadSummaries(levelStorage.getLevelList()).get();
        } catch (LevelStorageException | InterruptedException | ExecutionException e) {
            YourItemsToNewWorlds.LOGGER.error("Couldn't load level list.");
            MinecraftClient.getInstance().setScreen(new FatalErrorScreen(Text.translatable("selectWorld.unable_to_load"), Text.of(e.getMessage())));
            worlds = new ArrayList<>();
        }
        screen.onSelectedInstanceChanged();
    }

    public Path getPathOfWorld(int index){
        return selectedInstancePath.resolve("saves"+worlds.get(index).getName());
    }

    public Path getPathOfWorld(LevelSummary summary){
        return selectedInstancePath.resolve("saves"+summary.getName());
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
