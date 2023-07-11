package net.trueHorse.yourItemsToNewWorlds.gui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.*;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.trueHorse.yourItemsToNewWorlds.YourItemsToNewWorlds;
import net.trueHorse.yourItemsToNewWorlds.io.ItemImporter;
import net.trueHorse.yourItemsToNewWorlds.screenHandlers.ImportItemScreenHandler;

import java.io.File;
import java.util.*;
import java.util.function.Consumer;

public class ImportItemsScreen extends Screen {

    private final Identifier textureSheet = new Identifier("your_items_to_new_worlds","textures/gui/import_items_screen.png");
    private final Consumer<Optional<ArrayList<ItemStack>>> applier;
    private final List<String> playerNames = new ArrayList<>();
    private final String[] searchLocationDeterminationModeIDs = {"transfer_items.your_items_to_new_worlds.spawn_point",
            "transfer_items.your_items_to_new_worlds.most_item_containers",
            "transfer_items.your_items_to_new_worlds.longest_inhabitation",
            "transfer_items.your_items_to_new_worlds.coordinates"};
    private TextFieldWidget worldPathWidget;
    private CyclingButtonWidget<String> playerNameWidget;
    private CyclingButtonWidget<String> searchLocationModeWidget;
    private final TextFieldWidget[] coordFields = new TextFieldWidget[3];
    private ButtonWidget searchButton;
    private CyclingButtonWidget<Boolean> selectAllButton;

    private TexturedButtonWidget leftArrowButton;
    private TexturedButtonWidget rightArrowButton;
    private final ArrayList<TexturedItemButtonWidget> itemSelectButtons= new ArrayList<>();
    private final Screen parent;
    private final ImportItemScreenHandler handler = new ImportItemScreenHandler();
    private String lastWorldPathString = "";
    private int gridPage = 0;

    public ImportItemsScreen(Screen parent, Consumer<Optional<ArrayList<ItemStack>>> applier){
        super(Text.of("tempTransferItemsText"));
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

        worldPathWidget = new TextFieldWidget(this.textRenderer,minDistanceFromEdge,margin,this.width-2*minDistanceFromEdge,20,Text.of("tempPathText"));
        worldPathWidget.setPlaceholder(Text.of("tempPathText"));
        worldPathWidget.setMaxLength(200);
        worldPathWidget.setText(lastWorldPathString);
        worldPathWidget.setChangedListener(text -> updateUI(text));
        widgets.add(worldPathWidget);
        setInitialFocus(worldPathWidget);

        File potentialWorldPath = new File(worldPathWidget.getText());
        YourItemsToNewWorlds.LOGGER.warn(String.valueOf(potentialWorldPath.exists()));
        if(potentialWorldPath.exists()){
            //TODO check if actually a Minecraft world folder
            //test names
            playerNames.add("Horse");
            playerNames.add("Jo");

            playerNameWidget = CyclingButtonWidget.builder(Text::of).values(playerNames).build(this.width/2-50,worldPathWidget.getY()+worldPathWidget.getHeight()+ margin,100,20,Text.of("tempPlayerNameText"));
            widgets.add(playerNameWidget);

            final int coordRowStartX = (this.width-(3*(50+ margin)+150))/2;
            final int coordRowY = playerNameWidget.getY()+playerNameWidget.getHeight()+ margin;
            searchLocationModeWidget = CyclingButtonWidget.<String>builder(Text::translatable).values(searchLocationDeterminationModeIDs).build(coordRowStartX,coordRowY,150,20,Text.of("tempLocationModeText"),
                    (button,val)->{
                button.setMessage(Text.translatable(val));
                setCoordFieldsEditability(Objects.equals(val, searchLocationDeterminationModeIDs[3]));
                    });
            widgets.add(searchLocationModeWidget);
            coordFields[0] = new TextFieldWidget(this.textRenderer,searchLocationModeWidget.getX()+searchLocationModeWidget.getWidth()+ margin,coordRowY,50,20,Text.of("tempXText"));
            coordFields[1] = new TextFieldWidget(this.textRenderer,coordFields[0].getX()+coordFields[0].getWidth()+ margin,coordRowY,50,20,Text.of("tempYText"));
            coordFields[2] = new TextFieldWidget(this.textRenderer,coordFields[1].getX()+coordFields[1].getWidth()+ margin,coordRowY,50,20,Text.of("tempZText"));
            for(TextFieldWidget coordField:coordFields){
                //checks, if text only consists of digits
                coordField.setTextPredicate(string -> string.matches("\\d*"));
            }
            setCoordFieldsEditability(false);
            widgets.addAll(List.of(coordFields));

            searchButton = ButtonWidget.builder(Text.of("tempSearchText"),button-> generateAndDisplayGridArea()).dimensions(this.width/2-75,searchLocationModeWidget.getY()+searchLocationModeWidget.getHeight()+ margin,150,20).build();
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
                            handler.toggleSelection(itemSelectButtons.indexOf(button)+gridPage*itemSelectButtons.size());
                            YourItemsToNewWorlds.LOGGER.warn("pressed item "+((TexturedItemButtonWidget)button).itemStack);},ItemStack.EMPTY);
                    selectButton.visible = false;
                    itemSelectButtons.add(selectButton);
                }
            }

            selectAllButton = CyclingButtonWidget.onOffBuilder(false).build(this.width-minDistanceFromEdge-30-rightArrowButton.getWidth()-additionalGridXMargin,searchButton.getY()+searchButton.getHeight()+margin+additionalGridYMargin-12,30,12,Text.of("Select All"),
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
        }

        widgets.add(addDrawableChild(ButtonWidget.builder(Text.translatable("controls.resetAll"), button -> close()).dimensions(this.width / 2 - 155, this.height-29, 150, 20).build()));
        widgets.add(ButtonWidget.builder(ScreenTexts.DONE, button -> applyAndClose()).dimensions(this.width / 2 - 155 + 160, this.height-29, 150, 20).build());

        widgets.forEach(this::addDrawableChild);
    }

    public void setCoordFieldsEditability(boolean coordsEditable){
        for(TextFieldWidget coordField:coordFields){
            coordField.setEditable(coordsEditable);
            coordField.active = coordsEditable;
        }
    }

    public void generateAndDisplayGridArea(){
        //TODO use coord fields
        //handler.initImportableItemStacksWith(ItemImporter.readItemsFromOtherWorld(Arrays.binarySearch(searchLocationDeterminationModeIDs,searchLocationModeWidget.getValue()),new BlockPos(Integer.parseInt(coordFields[0].getText()),Integer.parseInt(coordFields[1].getText()),Integer.parseInt(coordFields[2].getText()))));
        handler.initImportableItemStacksWith(ItemImporter.readItemsFromOtherWorld(Arrays.asList(searchLocationDeterminationModeIDs).indexOf(searchLocationModeWidget.getValue()),new BlockPos(0,0,0)));
        refreshGridArea();
        selectAllButton.visible = true;
    }

    public void refreshGridArea(){
        leftArrowButton.visible = !(gridPage==0);

        int pageItemCount;
        YourItemsToNewWorlds.LOGGER.warn(String.valueOf(handler.getImportableItems().size()));
        if(handler.getImportableItems().size() - gridPage * itemSelectButtons.size()<=itemSelectButtons.size()){
            pageItemCount = handler.getImportableItems().size() - gridPage * itemSelectButtons.size();
            rightArrowButton.visible = false;
            for(int i=pageItemCount-1;i<itemSelectButtons.size();i++){
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
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta){
        renderBackground(context);
        super.render(context,mouseX,mouseY,delta);
    }

    @Override
    public void tick(){
        worldPathWidget.tick();
    }

    @Override
    public void close(){
        client.setScreen(parent);
    }

    public void applyAndClose(){
        applier.accept(Optional.empty());
        close();
    }

    private void updateUI(String newTextFieldContent){
        lastWorldPathString = newTextFieldContent;
        this.clearAndInit();
    }
}
