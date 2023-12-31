package net.trueHorse.yourItemsToNewWorlds.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.*;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.trueHorse.yourItemsToNewWorlds.YourItemsToNewWorlds;
import net.trueHorse.yourItemsToNewWorlds.gui.handlers.ImportItemScreenHandler;
import net.trueHorse.yourItemsToNewWorlds.io.ItemImporter;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

public class ImportItemsScreen extends Screen {

    private final Identifier textureSheet = new Identifier("your_items_to_new_worlds","textures/gui/import_items_screen.png");
    private final BiConsumer<ArrayList<ItemStack>,ImportItemsScreen> applier;
    private final String[] searchLocationDeterminationModeIDs = {"transfer_items.your_items_to_new_worlds.spawn_point",
            "transfer_items.your_items_to_new_worlds.most_item_containers",
            "transfer_items.your_items_to_new_worlds.longest_inhabitation",
            "transfer_items.your_items_to_new_worlds.coordinates"};
    private ButtonWidget selectWorldButton;
    private CyclingButtonWidget<String> playerNameWidget;
    private CyclingButtonWidget<ItemImporter.SearchLocationDeterminationMode> searchLocationModeWidget;
    private final TextFieldWidget[] coordFields = new TextFieldWidget[2];
    private TextFieldWidget radiusWidget;
    private ButtonWidget searchButton;
    private CyclingButtonWidget<Boolean> selectAllButton;

    private TexturedButtonWidget leftArrowButton;
    private TexturedButtonWidget rightArrowButton;
    private final ArrayList<TexturedItemButtonWidget> itemSelectButtons= new ArrayList<>();
    private TextWidget noItemsTextWidget;
    private TextWidget searchingTextWidget;
    private final Screen parent;
    private final ImportItemScreenHandler handler = new ImportItemScreenHandler(this);
    private int gridPage = 0;

    public ImportItemsScreen(Screen parent, BiConsumer<ArrayList<ItemStack>,ImportItemsScreen> applier){
        super(Text.translatable("transfer_items.your_items_to_new_worlds.select_transfer_items"));
        this.applier = applier;
        this.parent = parent;
    }

    @Override
    protected void init(){
        itemSelectButtons.clear();
        super.init();

        final int margin = 10;
        final int minDistanceFromEdge = 25;

        ArrayList<ClickableWidget> widgets = new ArrayList<>();

        selectWorldButton = ButtonWidget.builder(handler.getSelectedWorldPath()==null ?Text.translatable("transfer_items.your_items_to_new_worlds.no_world_selected"):Text.of(handler.getSelectedWorldPath().getFileName().toString()),
                button -> client.setScreen(new ImportWorldSelectionScreen(Text.translatable("narrator.your_items_to_new_worlds.select_import_world"),this, path -> handler.setSelectedWorldPath(path)))).dimensions(minDistanceFromEdge,margin,this.width-2*minDistanceFromEdge,20).build();
        widgets.add(selectWorldButton);

        if(handler.getSelectedWorldPath()!=null){
            playerNameWidget = CyclingButtonWidget.builder(Text::of).values(handler.getPlayerNames()).build(this.width/2-50,selectWorldButton.getY()+selectWorldButton.getHeight()+ margin,100,20,Text.translatable("transfer_items.your_items_to_new_worlds.player_name"),
                    (button,val)-> {
                        button.setMessage(Text.of(val));
                        handler.setSelectedPlayerName(val);
                    });
            playerNameWidget.setMessage(Text.of(playerNameWidget.getValue()));
            if(!handler.wasNameRequestSucessful()){
                playerNameWidget.setTooltip(Tooltip.of(Text.translatable("transfer_items.your_items_to_new_worlds.request_error_tooltip")));
            }else{
                playerNameWidget.setTooltip(Tooltip.of(Text.translatable("transfer_items.your_items_to_new_worlds.player_button_explanation")));
            }
            widgets.add(playerNameWidget);
            handler.setSelectedPlayerName(playerNameWidget.getValue());

            final int coordRowY = playerNameWidget.getY()+playerNameWidget.getHeight()+ margin;
            searchLocationModeWidget = CyclingButtonWidget.<ItemImporter.SearchLocationDeterminationMode>builder(val -> Text.translatable(searchLocationDeterminationModeIDs[val.ordinal()])).values(ItemImporter.SearchLocationDeterminationMode.values()).build(Math.max(minDistanceFromEdge,this.width/2-152),coordRowY,150,20,Text.of(""),
                    (button,val)->{
                button.setMessage(Text.translatable(searchLocationDeterminationModeIDs[val.ordinal()]));
                setCoordFieldsEditability(val.ordinal()==3);
                button.setTooltip(Tooltip.of(Text.translatable("transfer_items.your_items_to_new_worlds.mode_button_explanation")));
                handler.setSearchLocationDeterminationMode(val);
                    });
            searchLocationModeWidget.setMessage(Text.translatable(searchLocationDeterminationModeIDs[searchLocationModeWidget.getValue().ordinal()]));
            searchLocationModeWidget.setTooltip(Tooltip.of(Text.translatable("transfer_items.your_items_to_new_worlds.mode_button_explanation")));
            widgets.add(searchLocationModeWidget);
            handler.setSearchLocationDeterminationMode(searchLocationModeWidget.getValue());

            coordFields[0] = new TextFieldWidget(this.textRenderer,searchLocationModeWidget.getX()+searchLocationModeWidget.getWidth()+ margin,coordRowY,43,20,Text.of("X"));
            coordFields[1] = new TextFieldWidget(this.textRenderer,coordFields[0].getX()+coordFields[0].getWidth()+ margin,coordRowY,43,20,Text.of("Z"));
            for(TextFieldWidget coordField:coordFields){
                //checks, if text only consists of digits
                coordField.setTextPredicate(string -> string.matches("(^(-|\\d|))\\d*"));
                coordField.setPlaceholder(coordField.getMessage());
                coordField.setChangedListener(string -> {
                    if(!string.isEmpty()&& !string.equals("-")){
                        handler.setCoordinate(Integer.parseInt(string),coordField.getMessage().getString());
                    }
                });
            }
            setCoordFieldsEditability(false);
            widgets.addAll(List.of(coordFields));

            radiusWidget = new TextFieldWidget(this.textRenderer,coordFields[1].getX()+coordFields[0].getWidth()+ margin*2,coordRowY,40,20,Text.translatable("transfer_items.your_items_to_new_worlds.radius_from_chunk"));
            radiusWidget.setTextPredicate(string -> string.matches("\\d*"));
            radiusWidget.setTooltip(Tooltip.of(radiusWidget.getMessage()));
            radiusWidget.setPlaceholder(Text.translatable("transfer_items.your_items_to_new_worlds.radius"));
            radiusWidget.setMaxLength(1);
            radiusWidget.setText("1");
            radiusWidget.setChangedListener(string -> {
                if(!string.isEmpty()){
                    handler.setSearchRadius(Integer.parseInt(string));
                }
            });
            widgets.add(radiusWidget);
            handler.setSearchRadius(Integer.parseInt(radiusWidget.getText()));

            searchButton = ButtonWidget.builder(Text.translatable("itemGroup.search"),button-> generateAndDisplayGridArea()).dimensions(this.width/2-75,searchLocationModeWidget.getY()+searchLocationModeWidget.getHeight()+ margin,150,20).build();
            widgets.add(searchButton);

            final int pixelsBetweenSearchAndBack = this.height-29-(searchButton.getY()+searchButton.getHeight());
            final int pageArrowY = searchButton.getY()+searchButton.getHeight()+(pixelsBetweenSearchAndBack)/2-9;
            leftArrowButton = new TexturedButtonWidget(minDistanceFromEdge,pageArrowY,12,17,14,2,18,textureSheet,
                    button -> {gridPage--;
                    refreshGridArea();});
            leftArrowButton.visible = false;
            widgets.add(leftArrowButton);

            rightArrowButton = new TexturedButtonWidget(this.width- minDistanceFromEdge -12,pageArrowY,12,17,0,2,18,textureSheet,
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
                    selectButton.visible = false;
                    itemSelectButtons.add(selectButton);
                }
            }

            selectAllButton = CyclingButtonWidget.onOffBuilder(false).build(this.width-minDistanceFromEdge-30-rightArrowButton.getWidth()-additionalGridXMargin,searchButton.getY()+searchButton.getHeight()+margin+additionalGridYMargin-12,30,12,Text.translatable("transfer_items.your_items_to_new_worlds.select_all"),
                    (button,selectAll)->{
                        if(selectAll){
                            button.setMessage(Text.translatable("gui.none"));
                        }else {
                            button.setMessage(Text.translatable("gui.all"));
                        }
                        handler.setAllSelections(selectAll);
                        refreshGridArea();
                    });
            selectAllButton.setMessage(Text.translatable("gui.all"));
            selectAllButton.visible = false;

            widgets.add(selectAllButton);
            widgets.addAll(itemSelectButtons);

            noItemsTextWidget = new TextWidget(this.width/2-100,pageArrowY,200,20,Text.translatable("transfer_items.your_items_to_new_worlds.no_items_found"), MinecraftClient.getInstance().textRenderer);
            noItemsTextWidget.visible = false;
            widgets.add(noItemsTextWidget);

            searchingTextWidget = new TextWidget(this.width/2-50,pageArrowY,100,20,Text.translatable("transfer_items.your_items_to_new_worlds.searching"), MinecraftClient.getInstance().textRenderer);
            searchingTextWidget.visible = false;
            widgets.add(searchingTextWidget);
        }

        widgets.add(addDrawableChild(ButtonWidget.builder(Text.translatable("gui.cancel"), button -> close()).dimensions(this.width / 2 + 5, this.height-29, 150, 20).build()));
        widgets.add(ButtonWidget.builder(ScreenTexts.DONE, button -> applyAndClose()).dimensions(this.width / 2 - 155, this.height-29, 150, 20).build());

        widgets.forEach(this::addDrawableChild);
    }

    public void setCoordFieldsEditability(boolean coordsEditable){
        for(TextFieldWidget coordField:coordFields){
            coordField.setEditable(coordsEditable);
            coordField.active = coordsEditable;
            coordField.setPlaceholder(coordsEditable ? coordField.getMessage():Text.of(""));
        }
    }

    @Override
    public void tick(){
        super.tick();
        if(handler.getSelectedWorldPath()!=null){
            for(TextFieldWidget field:coordFields){
                field.tick();
            }
            radiusWidget.tick();
        }
        handler.tick();
    }

    public void generateAndDisplayGridArea(){
        if(radiusWidget.getText().isEmpty()){
            searchButton.setTooltip(Tooltip.of(Text.translatable("transfer_items.your_items_to_new_worlds.missing_radius_tooltip")));
            return;
        }
        if(handler.getSearchLocationDeterminationMode().ordinal()==3) {
            for (TextFieldWidget coordField : coordFields) {
                if (coordField.getText().isEmpty()||coordField.getText().equals("-")) {
                    searchButton.setTooltip(Tooltip.of(Text.translatable("transfer_items.your_items_to_new_worlds.missing_coordinates_tooltip")));
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
            List<Text> tooltipLines = itemStack.getTooltip(null, TooltipContext.BASIC);
            for(Text line:tooltipLines){
                tooltipBuilder.append(line.getString()).append("\n");
            }
            tooltipBuilder.deleteCharAt(tooltipBuilder.length()-1);
            button.setTooltip(Tooltip.of(Text.of(tooltipBuilder.toString())));

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
        for(TextFieldWidget coordField:coordFields){
            coordField.setText(handler.getCoordinate(coordField.getMessage().getString()).toString());
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta){
        renderBackground(context);
        super.render(context,mouseX,mouseY,delta);
    }

    @Override
    public void close(){
        client.setScreen(parent);
    }

    public void applyAndClose(){
        applier.accept(handler.getSelectedItems(),this);
        close();
    }
}
