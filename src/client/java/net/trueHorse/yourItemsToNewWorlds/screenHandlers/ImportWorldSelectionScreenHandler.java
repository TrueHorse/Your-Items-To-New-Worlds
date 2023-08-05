package net.trueHorse.yourItemsToNewWorlds.screenHandlers;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.FatalErrorScreen;
import net.minecraft.text.Text;
import net.minecraft.world.level.storage.LevelStorage;
import net.minecraft.world.level.storage.LevelStorageException;
import net.minecraft.world.level.storage.LevelSummary;
import net.trueHorse.yourItemsToNewWorlds.YourItemsToNewWorlds;
import net.trueHorse.yourItemsToNewWorlds.gui.ImportWorldSelectionScreen;
import net.trueHorse.yourItemsToNewWorlds.io.InstancesFileIO;
import org.lwjgl.util.tinyfd.TinyFileDialogs;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class ImportWorldSelectionScreenHandler {

    private final ImportWorldSelectionScreen screen;
    private List<LevelSummary> worlds = new ArrayList<>();
    private List<Path> instancePaths;
    private Path selectedInstancePath = null;
    private final InstancesFileIO instancesFileIO = new InstancesFileIO();

    private LevelSummary selectedWorld;

    public ImportWorldSelectionScreenHandler(ImportWorldSelectionScreen screen){
        this.screen = screen;
        instancesFileIO.loadInstances();
    }

    public void chooseNewInstance(){
        String folderPath = TinyFileDialogs.tinyfd_selectFolderDialog(Text.of("Add instance folder").getString(),MinecraftClient.getInstance().runDirectory.getAbsolutePath());
        if(folderPath != null){
            this.addInstance(new File(folderPath).toPath());
        }
        instancesFileIO.saveInstances(instancePaths);
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
        return selectedInstancePath.resolve("saves/"+worlds.get(index).getName());
    }

    public Path getPathOfWorld(LevelSummary summary){
        return selectedInstancePath.resolve("saves/"+summary.getName());
    }

    public void addInstance(Path instance){
        instancePaths.add(instance);
        screen.onInstancesChanged();
    }

    public List<Path> getInstances(){
        return instancePaths;
    }

    public Path getSelectedInstancePath(){
        return selectedInstancePath;
    }

    public List<LevelSummary> getWorlds() {
        return worlds;
    }

    public void setWorlds(List<LevelSummary> worlds) {
        this.worlds = worlds;
    }

    public LevelSummary getSelectedWorld() {
        return selectedWorld;
    }

    public void setSelectedWorld(LevelSummary selectedWorld) {
        this.selectedWorld = selectedWorld;
    }
}
