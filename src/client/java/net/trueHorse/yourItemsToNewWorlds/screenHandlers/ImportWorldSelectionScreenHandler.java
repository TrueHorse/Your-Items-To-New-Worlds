package net.trueHorse.yourItemsToNewWorlds.screenHandlers;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.FatalErrorScreen;
import net.minecraft.text.Text;
import net.minecraft.world.level.storage.LevelStorage;
import net.minecraft.world.level.storage.LevelStorageException;
import net.minecraft.world.level.storage.LevelSummary;
import net.trueHorse.yourItemsToNewWorlds.YourItemsToNewWorlds;
import net.trueHorse.yourItemsToNewWorlds.gui.ImportWorldSelectionScreen;
import org.lwjgl.util.tinyfd.TinyFileDialogs;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class ImportWorldSelectionScreenHandler {

    private final ImportWorldSelectionScreen screen;
    private List<LevelSummary> worlds = new ArrayList<>();
    private List<Path> instancePaths;
    private Path selectedInstancePath = null;
    private static final Path INSTANCES_FILE_PATH = MinecraftClient.getInstance().runDirectory.toPath().resolve("config/Your Items to New Worlds/instances.txt");
    private static final Path CURRENT_INSTANCE_PATH = MinecraftClient.getInstance().runDirectory.toPath();

    private LevelSummary selectedWorld;

    public ImportWorldSelectionScreenHandler(ImportWorldSelectionScreen screen){
        this.screen = screen;
        loadInstances();
    }

    public void chooseNewInstance(){
        String folderPath = TinyFileDialogs.tinyfd_selectFolderDialog(Text.of("Add instance folder").getString(),MinecraftClient.getInstance().runDirectory.getAbsolutePath());
        if(folderPath != null){
            this.addInstance(new File(folderPath).toPath());
        }
        saveInstances();
    }

    public void saveInstances(){
       File instancesFile = INSTANCES_FILE_PATH.toFile();

       if(!instancesFile.getParentFile().exists()){
           instancesFile.getParentFile().mkdirs();
       }

       StringBuilder builder = new StringBuilder();
       builder.append(instancePaths.isEmpty() ? "":instancePaths.get(0));
       for(int i = 1; i<instancePaths.size();i++){
           builder.append(',').append(instancePaths.get(i));
       }

        try {
            FileWriter confWriter = new FileWriter(instancesFile);
            confWriter.write(builder.toString());
            confWriter.close();
        } catch (IOException e) {
            YourItemsToNewWorlds.LOGGER.error("Saving instances failed.");
            e.printStackTrace();
        }
    }

    public void loadInstances(){
        instancePaths = new ArrayList<>();
        if(!INSTANCES_FILE_PATH.toFile().exists()){
            instancePaths.add(CURRENT_INSTANCE_PATH);
        }else {
            String pathsString;
            try {
                pathsString = Files.readAllLines(INSTANCES_FILE_PATH).get(0);
            } catch (IOException e) {
                YourItemsToNewWorlds.LOGGER.error("Failed to load instances.");
                e.printStackTrace();
                instancePaths.add(CURRENT_INSTANCE_PATH);
                return;
            }

            String[] paths = pathsString.split(",");
            Arrays.stream(paths).map(path -> new File(path).toPath()).forEach(instancePaths::add);
        }
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
