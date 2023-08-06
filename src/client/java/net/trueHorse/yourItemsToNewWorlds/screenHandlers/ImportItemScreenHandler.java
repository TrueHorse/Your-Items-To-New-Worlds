package net.trueHorse.yourItemsToNewWorlds.screenHandlers;

import com.google.gson.JsonParseException;
import net.minecraft.item.ItemStack;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.math.BlockPos;
import net.trueHorse.yourItemsToNewWorlds.YourItemsToNewWorlds;
import net.trueHorse.yourItemsToNewWorlds.io.ItemImporter;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class ImportItemScreenHandler {

    private ArrayList<ItemStack> importableItemStacks = new ArrayList<>();
    private boolean[] itemSelected;
    private final Map<String,String> playerIdNames = new HashMap<>();
    private final Map<Integer,ArrayList<ItemStack>> itemCache = new HashMap<>();
    private Path selectedWorldPath;

    public void initImportableItemStacks(String playerName, int searchMode){
        initImportableItemStacks(playerName,searchMode,null);
    }

    public void initImportableItemStacks(String playerName, int searchMode, BlockPos chosenCoords){
        if(itemCache.containsKey(searchMode)){
            importableItemStacks=itemCache.get(searchMode);
        }else {
            importableItemStacks = ItemImporter.readItemsFromOtherWorld(selectedWorldPath,getUuid(playerName),searchMode,chosenCoords);
            itemCache.put(searchMode,importableItemStacks);
            itemSelected = new boolean[importableItemStacks.size()];
            Arrays.fill(itemSelected, false);
        }
    }

    public void clearCache(){
        itemCache.clear();
    }

    //@return if all name requests where successful
    public boolean initPlayerNames(){
        AtomicBoolean success = new AtomicBoolean(true);
        File playerDataFolder = new File(selectedWorldPath+"\\playerdata");
        ArrayList<String> uuids;
        try {
            uuids = new ArrayList<>(Arrays.stream(playerDataFolder.list()).map(name -> name.substring(0,name.lastIndexOf("."))).toList());
        }catch (NullPointerException e){
            uuids = new ArrayList<>();
        }

        HttpClient client = HttpClient.newHttpClient();
        uuids.forEach(uuid ->{
            try {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create("https://sessionserver.mojang.com/session/minecraft/profile/" + uuid))
                        .GET()
                        .timeout(Duration.of(5, ChronoUnit.SECONDS))
                        .build();
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                playerIdNames.put(uuid,JsonHelper.deserialize(response.body()).get("name").getAsString());
            }catch (IOException | InterruptedException | NullPointerException | JsonParseException e){//timeout is subclass of IO
                YourItemsToNewWorlds.LOGGER.error("Player name request failed.");
                YourItemsToNewWorlds.LOGGER.error(e.getMessage());
                playerIdNames.put(uuid,uuid);
                success.set(false);
            }
        });
        return success.get();
    }

    public ArrayList<ItemStack> getSelectedItems(){
        ArrayList<ItemStack> selectedItems = new ArrayList<>();
        for(int i=0;i<importableItemStacks.size();i++){
            if(itemSelected[i]){
                selectedItems.add(importableItemStacks.get(i));
            }
        }
        return selectedItems;
    }

    public void setAllSelections(boolean val){
        Arrays.fill(itemSelected,val);
    }

    public ArrayList<String> getPlayerNames(){
        return new ArrayList<>(playerIdNames.values());
    }

    public String getPlayerName(String uuid){
        return playerIdNames.get(uuid);
    }

    public String getUuid(String playerName){
        for (Map.Entry<String, String> entry : playerIdNames.entrySet()) {
            if (entry.getValue().equals(playerName)) {
                return entry.getKey();
            }
        }
        return null;
    }

    public ArrayList<ItemStack> getImportableItems() {
        return importableItemStacks;
    }


    public void toggleSelection(int index){
        itemSelected[index] = !itemSelected[index];
    }

    public boolean[] getItemSelected() {
        return itemSelected;
    }

    public Path getSelectedWorldPath() {
        return selectedWorldPath;
    }

    public void setSelectedWorldPath(Path selectedWorldPath) {
        this.selectedWorldPath = selectedWorldPath;
    }
}
