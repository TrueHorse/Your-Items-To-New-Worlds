package net.trueHorse.yourItemsToNewWorlds.io;

import net.minecraft.client.MinecraftClient;
import net.trueHorse.yourItemsToNewWorlds.YourItemsToNewWorlds;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class InstancesFileIO {

    private static final Path INSTANCES_FILE_PATH = MinecraftClient.getInstance().runDirectory.toPath().resolve("config/Your Items to New Worlds/instances.txt");
    private static final Path CURRENT_INSTANCE_PATH = MinecraftClient.getInstance().runDirectory.toPath();

    public void saveInstances(List<Path> instancePaths){
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

    public List<Path> loadInstances(){
        List<Path> instancePaths = new ArrayList<>();
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
                return instancePaths;
            }

            String[] paths = pathsString.split(",");
            Arrays.stream(paths).map(path -> new File(path).toPath()).forEach(instancePaths::add);
        }
        return instancePaths;
    }
}
