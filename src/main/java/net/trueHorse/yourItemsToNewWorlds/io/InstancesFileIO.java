package net.trueHorse.yourItemsToNewWorlds.io;

import net.minecraft.client.Minecraft;
import net.trueHorse.yourItemsToNewWorlds.YourItemsToNewWorlds;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

public class InstancesFileIO {

    private static final Path INSTANCES_FILE_PATH = Minecraft.getInstance().gameDirectory.toPath().resolve("config/Your Items to New Worlds/instances.txt");
    private static final Path CURRENT_INSTANCE_PATH = Minecraft.getInstance().gameDirectory.toPath();

    public void saveInstances(InstanceList instanceList){
        File instancesFile = INSTANCES_FILE_PATH.toFile();

        if(!instancesFile.getParentFile().exists()){
            instancesFile.getParentFile().mkdirs();
        }

        StringBuilder builder = new StringBuilder();
        List<Path> instancePaths = instanceList.get().stream().map(LauncherMinecraftInstance::path).toList();
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

    public InstanceList loadInstances(){
        InstanceList instances = new InstanceList();
        if(!INSTANCES_FILE_PATH.toFile().exists()){
            instances.add(CURRENT_INSTANCE_PATH);
        }else {
            String pathsString;
            try {
                pathsString = Files.readAllLines(INSTANCES_FILE_PATH).get(0);
            } catch (IOException e) {
                YourItemsToNewWorlds.LOGGER.error("Failed to load instances.");
                instances.add(CURRENT_INSTANCE_PATH);
                return instances;
            }

            String[] paths = pathsString.split(",");
            Arrays.stream(paths).map(path -> new File(path).toPath()).forEach(instances::add);
        }
        return instances;
    }
}
