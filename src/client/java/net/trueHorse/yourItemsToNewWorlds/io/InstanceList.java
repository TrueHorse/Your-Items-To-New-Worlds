package net.trueHorse.yourItemsToNewWorlds.io;

import net.trueHorse.yourItemsToNewWorlds.YourItemsToNewWorlds;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class InstanceList {

    private final List<LauncherMinecraftInstance> instances = new ArrayList<>();

    private Path getMinecraftPath(Path launcherInstancePath){
        Path[] possiblePaths = {launcherInstancePath.resolve("minecraft"),launcherInstancePath.resolve(".minecraft")};
        for(Path posPath:possiblePaths){
            if(posPath.toFile().exists()){
                YourItemsToNewWorlds.LOGGER.info("path changed");
                return posPath;
            }
        }
        return launcherInstancePath;
    }

    public void add(Path launcherInstancePath){
        instances.add(new LauncherMinecraftInstance(launcherInstancePath,getMinecraftPath(launcherInstancePath)));
    }

    public LauncherMinecraftInstance get(int i){
        return instances.get(i);
    }

    public List<LauncherMinecraftInstance> get(){
        return instances;
    }

    public void remove(Path launcherInstancePath) {
        instances.removeIf((instance)->instance.path()==launcherInstancePath);
    }
}
