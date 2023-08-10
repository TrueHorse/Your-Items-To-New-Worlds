package net.trueHorse.yourItemsToNewWorlds.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.trueHorse.yourItemsToNewWorlds.gui.handlers.ImportItemScreenHandler;
import net.trueHorse.yourItemsToNewWorlds.io.ItemImporter;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

public class ImportItemsScreen extends Screen {

    private final ResourceLocation textureSheet = new ResourceLocation("your_items_to_new_worlds","textures/gui/import_items_screen.png");
    private final BiConsumer<ArrayList<ItemStack>,ImportItemsScreen> applier;
    private final String[] searchLocationDeterminationModeIDs = {"transfer_items.your_items_to_new_worlds.spawn_point",
            "transfer_items.your_items_to_new_worlds.most_item_containers",
            "transfer_items.your_items_to_new_worlds.longest_inhabitation",
            "transfer_items.your_items_to_new_worlds.coordinates"};
    private Button selectWorldButton;
    private CycleButton<String> playerNameWidget;
    private CycleButton<ItemImporter.SearchLocationDeterminationMode> searchLocationModeWidget;
    private final EditBox[] coordFields = new EditBox[2];
    private EditBox radiusWidget;
    private Button searchButton;
    private CycleButton<Boolean> selectAllButton;

    private ImageButton leftArrowButton;
    private ImageButton rightArrowButton;
    private final ArrayList<TexturedItemButtonWidget> itemSelectButtons= new ArrayList<>();
    private StringWidget noItemsTextWidget;
    private StringWidget searchingTextWidget;
    private final Screen parent;
    private final ImportItemScreenHandler handler = new ImportItemScreenHandler(this);
    private int gridPage = 0;

    public ImportItemsScreen(Screen parent, BiConsumer<ArrayList<ItemStack>,ImportItemsScreen> applier){
        super(Component.translatable("transfer_items.your_items_to_new_worlds.select_transfer_items"));
        this.applier = applier;
        this.parent = parent;
    }

    @Override
    protected void init(){
        itemSelectButtons.clear();
        super.init();

        final int margin = 10;
        final int minDistanceFromEdge = 25;

        ArrayList<AbstractWidget> widgets = new ArrayList<>();

        selectWorldButton = Button.builder(handler.getSelectedWorldPath()==null ?Component.translatable("transfer_items.your_items_to_new_worlds.no_world_selected"):Component.literal(handler.getSelectedWorldPath().getFileName().toString()),
                button -> minecraft.setScreen(new ImportWorldSelectionScreen(Component.translatable("narrator.your_items_to_new_worlds.select_import_world"),this, path -> handler.setSelectedWorldPath(path)))).dimensions(minDistanceFromEdge,margin,this.width-2*minDistanceFromEdge,20).build();
        widgets.add(selectWorldButton);

        if(handler.getSelectedWorldPath()!=null){
            playerNameWidget = CycleButton.builder(Component::literal).withValues(handler.getPlayerNames()).create(this.width/2-50,selectWorldButton.getY()+selectWorldButton.getHeight()+ margin,100,20,Component.translatable("transfer_items.your_items_to_new_worlds.player_name"),
                    (button,val)-> {
                        button.setMessage(Component.literal(val));
                        handler.setSelectedPlayerName(val);
                    });
            playerNameWidget.setMessage(Component.literal(playerNameWidget.getValue()));
            if(!handler.wasNameRequestSucessful()){
                playerNameWidget.setTooltip(Tooltip.create(Component.translatable("transfer_items.your_items_to_new_worlds.request_error_tooltip")));
            }else{
                playerNameWidget.setTooltip(Tooltip.create(Component.translatable("transfer_items.your_items_to_new_worlds.player_button_explanation")));
            }
            widgets.add(playerNameWidget);
            handler.setSelectedPlayerName(playerNameWidget.getValue());

            final int coordRowY = playerNameWidget.getY()+playerNameWidget.getHeight()+ margin;
            searchLocationModeWidget = CycleButton.<ItemImporter.SearchLocationDeterminationMode>builder(val -> Component.translatable(searchLocationDeterminationModeIDs[val.ordinal()])).withValues(ItemImporter.SearchLocationDeterminationMode.values()).create(Math.max(minDistanceFromEdge,this.width/2-152),coordRowY,150,20,Component.literal(""),
                    (button,val)->{
                button.setMessage(Component.translatable(searchLocationDeterminationModeIDs[val.ordinal()]));
                setCoordFieldsEditability(val.ordinal()==3);
                button.setTooltip(Tooltip.create(Component.translatable("transfer_items.your_items_to_new_worlds.mode_button_explanation")));
                handler.setSearchLocationDeterminationMode(val);
                    });
            searchLocationModeWidget.setMessage(Component.translatable(searchLocationDeterminationModeIDs[searchLocationModeWidget.getValue().ordinal()]));
            searchLocationModeWidget.setTooltip(Tooltip.create(Component.translatable("transfer_items.your_items_to_new_worlds.mode_button_explanation")));
            widgets.add(searchLocationModeWidget);
            handler.setSearchLocationDeterminationMode(searchLocationModeWidget.getValue());

            coordFields[0] = new EditBox(this.font,searchLocationModeWidget.getX()+searchLocationModeWidget.getWidth()+ margin,coordRowY,43,20,Component.literal("X"));
            coordFields[1] = new EditBox(this.font,coordFields[0].getX()+coordFields[0].getWidth()+ margin,coordRowY,43,20,Component.literal("Z"));
            for(EditBox coordField:coordFields){
                //checks, if text only consists of digits
                coordField.setFilter(string -> string.matches("(^(-|\\d|))\\d*"));
                coordField.setHint(coordField.getMessage());
                coordField.setResponder(string -> {
                    if(!string.isEmpty()&& !string.equals("-")){
                        handler.setCoordinate(Integer.parseInt(string),coordField.getMessage().getString());
                    }
                });
            }
            setCoordFieldsEditability(false);
            widgets.addAll(List.of(coordFields));

            radiusWidget = new EditBox(this.font,coordFields[1].getX()+coordFields[0].getWidth()+ margin*2,coordRowY,40,20,Component.translatable("transfer_items.your_items_to_new_worlds.radius_from_chunk"));
            radiusWidget.setFilter(string -> string.matches("\\d*"));
            radiusWidget.setTooltip(Tooltip.create(radiusWidget.getMessage()));
            radiusWidget.setHint(Component.translatable("transfer_items.your_items_to_new_worlds.radius"));
            radiusWidget.setMaxLength(1);
            radiusWidget.setValue("1");
            radiusWidget.setResponder(string -> {
                if(!string.isEmpty()){
                    handler.setSearchRadius(Integer.parseInt(string));
                }
            });
            widgets.add(radiusWidget);
            handler.setSearchRadius(Integer.parseInt(radiusWidget.getValue()));

            searchButton = Button.builder(Component.translatable("itemGroup.search"),button-> generateAndDisplayGridArea()).bounds(this.width/2-75,searchLocationModeWidget.getY()+searchLocationModeWidget.getHeight()+ margin,150,20).build();
            widgets.add(searchButton);

            final int pixelsBetweenSearchAndBack = this.height-29-(searchButton.getY()+searchButton.getHeight());
            final int pageArrowY = searchButton.getY()+searchButton.getHeight()+(pixelsBetweenSearchAndBack)/2-9;
            leftArrowButton = new ImageButton(minDistanceFromEdge,pageArrowY,12,17,14,2,18,textureSheet,
                    button -> {gridPage--;
                    refreshGridArea();});
            leftArrowButton.visible = false;
            widgets.add(leftArrowButton);

            rightArrowButton = new ImageButton(this.width- minDistanceFromEdge -12,pageArrowY,12,17,0,2,18,textureSheet,
                    button -> {gridPage++;
                        refreshGridArea();});
            rightArrowButton.visible = false;
            widgets.add(rightArrowButton);

            final int itemRows = (int) Math.floor((pixelsBetweenSearchAndBack - 2* margin)/25.0);
            final int itemColumns = (int) Math.floor((this.width-(minDistanceFromEdge +12)*2)/25.0);
            final int additionalGridYMargin = ((pixelsBetweenSearchAndBack-2* margin)%25)/2;
            final int additionalGridXMargin = ((this.width-(minDistanceFromEdge +12)*2)%25)/2;
            for(int i=0;i<itemRows;i++){
                for(int j=0;j<itemColumns;j++){
                    TexturedItemButtonWidget selectButton = new TexturedItemButtonWidget(minDistanceFromEdge +12+additionalGridXMargin+j*25,searchButton.getY()+searchButton.getHeight()+ margin +additionalGridYMargin+i*25,25,25,27,0,25,textureSheet,
                            button -> {((TexturedItemButtonWidget)button).toggle();
                            handler.toggleSelection(itemSelectButtons.indexOf(button)+gridPage*itemSelectButtons.size());},ItemStack.EMPTY);
                    selectButton.visable = false;
                    itemSelectButtons.add(selectButton);
                }
            }

            selectAllButton = CycleButton.onOffBuilder(false).create(this.width-minDistanceFromEdge-30-rightArrowButton.getWidth()-additionalGridXMargin,searchButton.getY()+searchButton.getHeight()+margin+additionalGridYMargin-12,30,12,Component.translatable("transfer_items.your_items_to_new_worlds.select_all"),
                    (button,selectAll)->{
                        if(selectAll){
                            button.setMessage(Component.translatable("gui.none"));
                        }else {
                            button.setMessage(Component.translatable("gui.all"));
                        }
                        handler.setAllSelections(selectAll);
                        refreshGridArea();
                    });
            selectAllButton.setMessage(Component.translatable("gui.all"));
            selectAllButton.visible = false;

            widgets.add(selectAllButton);
            widgets.addAll(itemSelectButtons);

            noItemsTextWidget = new StringWidget(this.width/2-100,pageArrowY,200,20,Component.translatable("transfer_items.your_items_to_new_worlds.no_items_found"), Minecraft.getInstance().font);
            noItemsTextWidget.visible = false;
            widgets.add(noItemsTextWidget);

            searchingTextWidget = new StringWidget(this.width/2-50,pageArrowY,100,20,Component.translatable("transfer_items.your_items_to_new_worlds.searching"), Minecraft.getInstance().font);
            searchingTextWidget.visible = false;
            widgets.add(searchingTextWidget);
        }

        widgets.add(addRenderableWidget(Button.builder(Component.translatable("gui.cancel"), button -> close()).bounds(this.width / 2 + 5, this.height-29, 150, 20).build()));
        widgets.add(Button.builder(CommonComponents.GUI_DONE, button -> applyAndClose()).bounds(this.width / 2 - 155, this.height-29, 150, 20).build());

        widgets.forEach(this::addRenderableWidget);
    }

    public void setCoordFieldsEditability(boolean coordsEditable){
        for(EditBox coordField:coordFields){
            coordField.setEditable(coordsEditable);
            coordField.active = coordsEditable;
            coordField.setHint(coordsEditable ? coordField.getMessage():Component.literal(""));
        }
    }

    @Override
    public void tick(){
        super.tick();
        if(handler.getSelectedWorldPath()!=null){
            for(EditBox field:coordFields){
                field.tick();
            }
            radiusWidget.tick();
        }
        handler.tick();
    }

    public void generateAndDisplayGridArea(){
        if(radiusWidget.getValue().isEmpty()){
            searchButton.setTooltip(Tooltip.create(Component.translatable("transfer_items.your_items_to_new_worlds.missing_radius_tooltip")));
            return;
        }
        if(handler.getSearchLocationDeterminationMode().ordinal()==3) {
            for (EditBox coordField : coordFields) {
                if (coordField.getValue().isEmpty()||coordField.getValue().equals("-")) {
                    searchButton.setTooltip(Tooltip.create(Component.translatable("transfer_items.your_items_to_new_worlds.missing_coordinates_tooltip")));
                    return;
                }
            }
        }
        handler.searchImportableItemStacks();
    }

    public void refreshGridArea(){
        leftArrowButton.visible = !(gridPage==0);

        int pageItemCount;
        if(handler.getImportableItems().size() - gridPage * itemSelectButtons.size()<=itemSelectButtons.size()){
            pageItemCount = handler.getImportableItems().size() - gridPage * itemSelectButtons.size();
            rightArrowButton.visible = false;
            for(int i=pageItemCount;i<itemSelectButtons.size();i++){
                itemSelectButtons.get(i).visible = false;
            }
        }else{
            pageItemCount = itemSelectButtons.size();
            rightArrowButton.visible = true;
        }

        for(int i=0;i<pageItemCount;i++){
            TexturedItemButtonWidget button = itemSelectButtons.get(i);
            ItemStack itemStack = handler.getImportableItems().get(i+gridPage*itemSelectButtons.size());

            button.setItemStack(itemStack);

            StringBuilder tooltipBuilder = new StringBuilder();
            List<Component> tooltipLines = itemStack.getTooltipLines(null, TooltipFlag.NORMAL);
            for(Component line:tooltipLines){
                tooltipBuilder.append(line.getString()).append("\n");
            }
            tooltipBuilder.deleteCharAt(tooltipBuilder.length()-1);
            button.setTooltip(Tooltip.create(Component.literal(tooltipBuilder.toString())));

            button.setToggled(handler.getItemSelected()[i+gridPage*itemSelectButtons.size()]);
            button.visible = true;
        }

        boolean noItems = pageItemCount == 0;
        selectAllButton.visible = !noItems;
        noItemsTextWidget.visible = noItems;
    }

    public void onSearchStatusChanged(boolean searching){
        searchingTextWidget.visible = searching;
        selectWorldButton.active = !searching;
        playerNameWidget.active = !searching;
        searchLocationModeWidget.active = !searching;
        setCoordFieldsEditability(!searching && handler.getSearchLocationDeterminationMode() == ItemImporter.SearchLocationDeterminationMode.COORDINATES);
        radiusWidget.active = !searching;
        searchButton.active = !searching;
        if(!searching){
            refreshGridArea();
        }
    }

    public void updateCoordinateFields(){
        for(EditBox coordField:coordFields){
            coordField.setValue(handler.getCoordinate(coordField.getMessage().getString()).toString());
        }
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta){
        renderBackground(context);
        super.render(context,mouseX,mouseY,delta);
    }

    @Override
    public void close(){
        minecraft.setScreen(parent);
    }

    public void applyAndClose(){
        applier.accept(handler.getSelectedItems(),this);
        close();
    }
}
