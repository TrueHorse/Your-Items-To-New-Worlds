package net.trueHorse.yourItemsToNewWorlds.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.*;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.trueHorse.yourItemsToNewWorlds.screenHandlers.ImportItemScreenHandler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class ImportItemsScreen extends Screen {

    private final Identifier textureSheet = new Identifier("your_items_to_new_worlds","textures/gui/import_items_screen.png");
    private final Consumer<ArrayList<ItemStack>> applier;
    private final String[] searchLocationDeterminationModeIDs = {"transfer_items.your_items_to_new_worlds.spawn_point",
            "transfer_items.your_items_to_new_worlds.most_item_containers",
            "transfer_items.your_items_to_new_worlds.longest_inhabitation",
            "transfer_items.your_items_to_new_worlds.coordinates"};
    private ButtonWidget selectWorldButton;
    private CyclingButtonWidget<String> playerNameWidget;
    private CyclingButtonWidget<String> searchLocationModeWidget;
    private final TextFieldWidget[] coordFields = new TextFieldWidget[3];
    private TextFieldWidget radiusWidget;
    private ButtonWidget searchButton;
    private CyclingButtonWidget<Boolean> selectAllButton;

    private TexturedButtonWidget leftArrowButton;
    private TexturedButtonWidget rightArrowButton;
    private final ArrayList<TexturedItemButtonWidget> itemSelectButtons= new ArrayList<>();
    private TextWidget noItemsTextWidget;
    private final Screen parent;
    private final ImportItemScreenHandler handler = new ImportItemScreenHandler();
    private int gridPage = 0;

    public ImportItemsScreen(Screen parent, Consumer<ArrayList<ItemStack>> applier){
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
                button -> client.setScreen(new ImportWorldSelectionScreen(Text.of("select import world"),this, path -> handler.setSelectedWorldPath(path)))).dimensions(minDistanceFromEdge,margin,this.width-2*minDistanceFromEdge,20).build();
        widgets.add(selectWorldButton);

        if(handler.getSelectedWorldPath()!=null){
            boolean successfulNameRequests = handler.initPlayerNames();

            playerNameWidget = CyclingButtonWidget.builder(Text::of).values(handler.getPlayerNames()).build(this.width/2-50,selectWorldButton.getY()+selectWorldButton.getHeight()+ margin,100,20,Text.translatable("transfer_items.your_items_to_new_worlds.player_name"),
                    (button,val)-> button.setMessage(Text.of(val)));
            playerNameWidget.setMessage(Text.of(playerNameWidget.getValue()));
            if(!successfulNameRequests){
                playerNameWidget.setTooltip(Tooltip.of(Text.translatable("transfer_items.your_items_to_new_worlds.request_error_tooltip")));
            }else{
                playerNameWidget.setTooltip(Tooltip.of(Text.translatable("transfer_items.your_items_to_new_worlds.player_button_explanation")));
            }
            widgets.add(playerNameWidget);

            final int coordRowY = playerNameWidget.getY()+playerNameWidget.getHeight()+ margin;
            searchLocationModeWidget = CyclingButtonWidget.<String>builder(Text::translatable).values(searchLocationDeterminationModeIDs).build(minDistanceFromEdge,coordRowY,150,20,Text.of(""),
                    (button,val)->{
                button.setMessage(Text.translatable(val));
                setCoordFieldsEditability(Objects.equals(val, searchLocationDeterminationModeIDs[3]));
                    });
            searchLocationModeWidget.setMessage(Text.translatable(searchLocationModeWidget.getValue()));
            searchLocationModeWidget.setTooltip(Tooltip.of(Text.translatable("transfer_items.your_items_to_new_worlds.mode_button_explanation")));
            widgets.add(searchLocationModeWidget);

            coordFields[0] = new TextFieldWidget(this.textRenderer,searchLocationModeWidget.getX()+searchLocationModeWidget.getWidth()+ margin,coordRowY,43,20,Text.of("X"));
            coordFields[1] = new TextFieldWidget(this.textRenderer,coordFields[0].getX()+coordFields[0].getWidth()+ margin,coordRowY,43,20,Text.of("Y"));
            coordFields[2] = new TextFieldWidget(this.textRenderer,coordFields[1].getX()+coordFields[1].getWidth()+ margin,coordRowY,43,20,Text.of("Z"));
            for(TextFieldWidget coordField:coordFields){
                //checks, if text only consists of digits
                coordField.setTextPredicate(string -> string.matches("\\d*"));
            }
            setCoordFieldsEditability(false);
            widgets.addAll(List.of(coordFields));

            radiusWidget = new TextFieldWidget(this.textRenderer,this.width-minDistanceFromEdge-43,coordRowY,40,20,Text.translatable("transfer_items.your_items_to_new_worlds.radius_from_chunk"));
            radiusWidget.setTextPredicate(string -> string.matches("\\d*"));
            radiusWidget.setMaxLength(1);
            radiusWidget.setText("1");
            widgets.add(radiusWidget);

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
        }

        widgets.add(addDrawableChild(ButtonWidget.builder(Text.translatable("gui.cancel"), button -> close()).dimensions(this.width / 2 + 5, this.height-29, 150, 20).build()));
        widgets.add(ButtonWidget.builder(ScreenTexts.DONE, button -> applyAndClose()).dimensions(this.width / 2 - 155, this.height-29, 150, 20).build());

        widgets.forEach(this::addDrawableChild);
    }

    public void setCoordFieldsEditability(boolean coordsEditable){
        for(TextFieldWidget coordField:coordFields){
            coordField.setEditable(coordsEditable);
            coordField.active = coordsEditable;
        }
    }

    public void generateAndDisplayGridArea(){
        int modeNumber = Arrays.asList(searchLocationDeterminationModeIDs).indexOf(searchLocationModeWidget.getValue());
        if(radiusWidget.getText().isEmpty()){
            searchButton.setTooltip(Tooltip.of(Text.translatable("transfer_items.your_items_to_new_worlds.missing_radius_tooltip")));
            return;
        }
        if(modeNumber==3) {
            for (TextFieldWidget coordField : coordFields) {
                if (coordField.getText().isEmpty()) {
                    searchButton.setTooltip(Tooltip.of(Text.translatable("transfer_items.your_items_to_new_worlds.missing_coordinates_tooltip")));
                    return;
                }
            }
            handler.initImportableItemStacks(playerNameWidget.getValue(), modeNumber,Integer.parseInt(radiusWidget.getText()), new BlockPos(Integer.parseInt(coordFields[0].getText()),Integer.parseInt(coordFields[1].getText()),Integer.parseInt(coordFields[2].getText())));
        }else{
            handler.initImportableItemStacks(playerNameWidget.getValue(),modeNumber,Integer.parseInt(radiusWidget.getText()));
        }
        refreshGridArea();
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
            button.setItemStack(handler.getImportableItems().get(i+gridPage*itemSelectButtons.size()));
            button.setToggled(handler.getItemSelected()[i+gridPage*itemSelectButtons.size()]);
            button.visible = true;
        }

        boolean noItems = pageItemCount == 0;
        selectAllButton.visible = !noItems;
        noItemsTextWidget.visible = noItems;
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
        applier.accept(handler.getSelectedItems());
        close();
    }
}
