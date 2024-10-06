package net.trueHorse.yourItemsToNewWorlds.gui.handlers;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.FatalErrorScreen;
import net.minecraft.text.Text;
import net.minecraft.world.level.storage.LevelStorage;
import net.minecraft.world.level.storage.LevelStorageException;
import net.minecraft.world.level.storage.LevelSummary;
import net.trueHorse.yourItemsToNewWorlds.YourItemsToNewWorlds;
import net.trueHorse.yourItemsToNewWorlds.gui.ImportWorldSelectionScreen;
import net.trueHorse.yourItemsToNewWorlds.io.InstanceList;
import net.trueHorse.yourItemsToNewWorlds.io.LauncherMinecraftInstance;
import net.trueHorse.yourItemsToNewWorlds.io.InstancesFileIO;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.util.tinyfd.TinyFileDialogs;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class ImportWorldSelectionScreenHandler {

    private final ImportWorldSelectionScreen screen;
    private List<LevelSummary> worlds = new ArrayList<>();
    private final InstanceList instances;
    private LauncherMinecraftInstance selectedInstance = null;
    private LevelSummary selectedWorld;
    private final InstancesFileIO instancesFileIO = new InstancesFileIO();
    private Path lastAddedInstance;

    public ImportWorldSelectionScreenHandler(ImportWorldSelectionScreen screen){
        this.screen = screen;
        instances = instancesFileIO.loadInstances();
    }

    public void chooseNewInstance(){
        String folderPath = TinyFileDialogs.tinyfd_selectFolderDialog(Text.translatable("transfer_items.your_items_to_new_worlds.add_instance").getString(),lastAddedInstance==null ? MinecraftClient.getInstance().runDirectory.getAbsolutePath():lastAddedInstance.toString());
        if(folderPath != null){
            Path instancePath = new File(folderPath).toPath();
            this.addInstance(instancePath);
            lastAddedInstance = instancePath;
        }
    }



    public void onInstanceSelected(@Nullable LauncherMinecraftInstance instance){
        selectedInstance = instance;
        if(instance!=null){
            Path minecraftPath = instance.minecraftPath();
            LevelStorage levelStorage = new LevelStorage(minecraftPath.resolve("saves"),minecraftPath.resolve("backups"),LevelStorage.createSymlinkFinder(minecraftPath.resolve("allowed_symlinks.txt")),MinecraftClient.getInstance().getDataFixer());
            try {
                worlds = levelStorage.loadSummaries(levelStorage.getLevelList()).get();
            } catch (LevelStorageException | InterruptedException | ExecutionException e) {
                YourItemsToNewWorlds.LOGGER.error("Couldn't load level list.");
                MinecraftClient.getInstance().setScreen(new FatalErrorScreen(Text.translatable("selectWorld.unable_to_load"), Text.of(e.getMessage())));
                worlds = new ArrayList<>();
            }
        }

        screen.onSelectedInstanceChanged();
    }

    public Path getPathOfWorld(int index){
        return selectedInstance.minecraftPath().resolve("saves/"+worlds.get(index).getName());
    }

    public Path getPathOfWorld(LevelSummary summary){
        return selectedInstance.minecraftPath().resolve("saves/"+summary.getName());
    }

    public void addInstance(Path instance){
        instances.add(instance);
        instancesFileIO.saveInstances(instances);
        screen.onInstancesChanged();
    }

    public void removeInstance(Path instance){
        instances.remove(instance);
        instancesFileIO.saveInstances(instances);
        screen.onInstancesChanged();
    }

    public InstanceList getInstances(){
        return instances;
    }

    public LauncherMinecraftInstance getSelectedInstance(){
        return selectedInstance;
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
