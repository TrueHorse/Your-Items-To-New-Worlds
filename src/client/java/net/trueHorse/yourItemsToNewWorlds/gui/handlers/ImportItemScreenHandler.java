package net.trueHorse.yourItemsToNewWorlds.gui.handlers;

import com.google.gson.JsonParseException;
import net.minecraft.item.ItemStack;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.trueHorse.yourItemsToNewWorlds.YourItemsToNewWorlds;
import net.trueHorse.yourItemsToNewWorlds.gui.ImportItemsScreen;
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
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;

public class ImportItemScreenHandler {

    private ArrayList<ItemStack> importableItemStacks = new ArrayList<>();
    private boolean[] itemSelected;
    private final Map<String,String> playerIdNames = new HashMap<>();
    private final Map<ItemSearchConfig,Pair<ArrayList<ItemStack>,ChunkPos>> itemCache = new HashMap<>();
    private boolean nameRequestSucessful;
    private Path selectedWorldPath;
    private String selectedPlayerName;
    private ItemImporter.SearchLocationDeterminationMode searchLocationDeterminationMode;
    private final BlockPos.Mutable chosenPos = new BlockPos.Mutable();
    private int searchRadius;
    private final ImportItemsScreen screen;
    private CompletableFuture<Pair<ChunkPos,ArrayList<ItemStack>>> importResult;

    public ImportItemScreenHandler(ImportItemsScreen screen){
        this.screen = screen;
    }

    public void searchImportableItemStacks(){
        ItemSearchConfig currentConfig = new ItemSearchConfig(selectedWorldPath,selectedPlayerName,searchLocationDeterminationMode,searchLocationDeterminationMode==ItemImporter.SearchLocationDeterminationMode.COORDINATES ? new ChunkPos(chosenPos) :null,searchRadius);
        if(itemCache.containsKey(currentConfig)){
            Pair<ArrayList<ItemStack>,ChunkPos> pair =itemCache.get(currentConfig);
            importableItemStacks = pair.getLeft();
            if(searchLocationDeterminationMode != ItemImporter.SearchLocationDeterminationMode.COORDINATES) {
                chosenPos.set(pair.getRight().getBlockPos(0, 0, 0));
                screen.updateCoordinateFields();
            }
        }else {
            screen.onSearchStatusChanged(true);
            importResult = CompletableFuture.supplyAsync(()->{
                ItemImporter importer = new ItemImporter(selectedWorldPath,playerIdNames.containsKey(selectedPlayerName) ? selectedPlayerName:getUuid(selectedPlayerName));
                ChunkPos searchChunkPos = importer.getSearchChunkPos(searchLocationDeterminationMode,searchRadius, chosenPos);
                ArrayList<ItemStack> importableItemStacks = importer.getPlayerItems();
                importableItemStacks.addAll(importer.getItemsInArea(searchChunkPos,searchRadius));
                return new Pair<>(searchChunkPos, importableItemStacks);
            });
        }
    }

    public void tick(){
        try {
            if (importResult != null && importResult.getNow(null) != null) {
                onItemSearchComplete(importResult.get());
            }
        }catch (InterruptedException|CompletionException| ExecutionException| CancellationException e){
            YourItemsToNewWorlds.LOGGER.error("An error occurred during item search.\n"+e.getMessage());
            onItemSearchComplete(new Pair<>(new ChunkPos(0,0),new ArrayList<>()));
        }
    }

    private void onItemSearchComplete(Pair<ChunkPos, ArrayList<ItemStack>> result){
        importResult = null;

        ChunkPos searchChunkPos = result.getLeft();
        importableItemStacks = result.getRight();
        itemSelected = new boolean[importableItemStacks.size()];
        Arrays.fill(itemSelected, false);
        if(searchLocationDeterminationMode != ItemImporter.SearchLocationDeterminationMode.COORDINATES){
            chosenPos.set(searchChunkPos.getBlockPos(0,0,0));
            screen.updateCoordinateFields();
        }
        itemCache.put(new ItemSearchConfig(selectedWorldPath,selectedPlayerName,searchLocationDeterminationMode,searchLocationDeterminationMode==ItemImporter.SearchLocationDeterminationMode.COORDINATES ? new ChunkPos(chosenPos) :null,searchRadius)
                ,new Pair<>(result.getRight(),result.getLeft()));

        screen.onSearchStatusChanged(false);
    }

    //@return if all name requests where successful
    public void initPlayerNames(){
        playerIdNames.clear();
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
        nameRequestSucessful = success.get();
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

    public void setCoordinate(int val, String coord){
        switch (coord) {
            case "X" -> chosenPos.setX(val);
            case "Y" -> chosenPos.setY(val);
            case "Z" -> chosenPos.setZ(val);
            default -> throw new IllegalArgumentException();
        }
    }

    public Integer getCoordinate(String coord){
        return switch (coord) {
            case "X" -> chosenPos.getX();
            case "Y" -> chosenPos.getY();
            case "Z" -> chosenPos.getZ();
            default -> throw new IllegalArgumentException();
        };
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
        this.initPlayerNames();
    }

    public String getSelectedPlayerName() {
        return selectedPlayerName;
    }

    public void setSelectedPlayerName(String selectedPlayerName) {
        this.selectedPlayerName = selectedPlayerName;
    }

    public ItemImporter.SearchLocationDeterminationMode getSearchLocationDeterminationMode() {
        return searchLocationDeterminationMode;
    }

    public void setSearchLocationDeterminationMode(ItemImporter.SearchLocationDeterminationMode searchLocationDeterminationMode) {
        this.searchLocationDeterminationMode = searchLocationDeterminationMode;
    }

    public int getSearchRadius() {
        return searchRadius;
    }

    public void setSearchRadius(int searchRadius) {
        this.searchRadius = searchRadius;
    }

    public boolean wasNameRequestSucessful() {
        return nameRequestSucessful;
    }
}
