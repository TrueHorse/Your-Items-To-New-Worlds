package net.trueHorse.yourItemsToNewWorlds.screenHandlers;

import net.minecraft.item.ItemStack;
import net.minecraft.util.JsonHelper;
import net.trueHorse.yourItemsToNewWorlds.YourItemsToNewWorlds;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class ImportItemScreenHandler {

    private ArrayList<ItemStack> importableItemStacks;
    private boolean[] itemSelected;
    private final Map<String,String> playerIdNames = new HashMap<>();

    public void initImportableItemStacksWith(ArrayList<ItemStack> itemStacks){
        importableItemStacks = itemStacks;
        itemSelected = new boolean[itemStacks.size()];
        Arrays.fill(itemSelected,false);
    }

    public void initPlayerNames(File worldFolder){
        File playerDataFolder = new File(worldFolder.getPath()+"\\playerdata");
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
            }catch (IOException | InterruptedException e){//timeout is subclass of IO
                YourItemsToNewWorlds.LOGGER.error("Player name request failed.");
                playerIdNames.put(uuid,uuid);
            }
        });
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
}
