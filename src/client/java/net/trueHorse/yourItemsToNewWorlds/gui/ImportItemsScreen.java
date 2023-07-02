package net.trueHorse.yourItemsToNewWorlds.gui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.*;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.trueHorse.yourItemsToNewWorlds.YourItemsToNewWorlds;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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
    private TexturedButtonWidget leftArrowButton;
    private TexturedButtonWidget rightArrowButton;
    private final ArrayList<TexturedButtonWidget> itemSelectButtons= new ArrayList<>();
    private final Screen parent;
    private final ArrayList<ItemStack> importableItems = new ArrayList<>();
    private boolean[] itemSelected;
    private String lastWorldPathString = "";

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
            searchLocationModeWidget = CyclingButtonWidget.<String>builder(Text::translatable).values(searchLocationDeterminationModeIDs).build(coordRowStartX,coordRowY,150,20,Text.of("tempLocationModeText"));
            widgets.add(searchLocationModeWidget);
            coordFields[0] = new TextFieldWidget(this.textRenderer,searchLocationModeWidget.getX()+searchLocationModeWidget.getWidth()+ margin,coordRowY,50,20,Text.of("tempXText"));
            coordFields[1] = new TextFieldWidget(this.textRenderer,coordFields[0].getX()+coordFields[0].getWidth()+ margin,coordRowY,50,20,Text.of("tempYText"));
            coordFields[2] = new TextFieldWidget(this.textRenderer,coordFields[1].getX()+coordFields[1].getWidth()+ margin,coordRowY,50,20,Text.of("tempZText"));
            widgets.addAll(List.of(coordFields));

            searchButton = ButtonWidget.builder(Text.of("tempSearchText"),button-> makeGridAreaVisable()).dimensions(this.width/2-75,searchLocationModeWidget.getY()+searchLocationModeWidget.getHeight()+ margin,150,20).build();
            widgets.add(searchButton);

            final int pixelsBetweenSearchAndBack = this.height-29-(searchButton.getY()+searchButton.getHeight());
            final int pageArrowY = searchButton.getY()+searchButton.getHeight()+(pixelsBetweenSearchAndBack)/2-9;
            leftArrowButton = new TexturedButtonWidget(minDistanceFromEdge,pageArrowY,12,17,14,2,18,textureSheet, button -> YourItemsToNewWorlds.LOGGER.warn("pressed left"));
            leftArrowButton.visible = false;
            widgets.add(leftArrowButton);
            rightArrowButton = new TexturedButtonWidget(this.width- minDistanceFromEdge -12,pageArrowY,12,17,0,2,18,textureSheet, button -> YourItemsToNewWorlds.LOGGER.warn("pressed right"));
            rightArrowButton.visible = false;
            widgets.add(rightArrowButton);

            final int itemRows = (int) Math.floor((pixelsBetweenSearchAndBack - 2* margin)/25.0);
            final int itemColumns = (int) Math.floor((this.width-(minDistanceFromEdge +12)*2)/25.0);
            final int additionalGridYMargin = ((pixelsBetweenSearchAndBack-2* margin)%25)/2;
            final int additionalGridXMargin = ((this.width-(minDistanceFromEdge +12)*2)%25)/2;
            for(int i=0;i<itemRows;i++){
                for(int j=0;j<itemColumns;j++){
                    TexturedButtonWidget selectButton = new TexturedButtonWidget(minDistanceFromEdge +12+additionalGridXMargin+j*25,searchButton.getY()+searchButton.getHeight()+ margin +additionalGridYMargin+i*25,25,25,27,0,25,textureSheet, button -> YourItemsToNewWorlds.LOGGER.warn("pressed item "+itemSelectButtons.indexOf(button)));
                    selectButton.visible = false;
                    itemSelectButtons.add(selectButton);
                }
            }
            widgets.addAll(itemSelectButtons);
        }

        widgets.add(addDrawableChild(ButtonWidget.builder(Text.translatable("controls.resetAll"), button -> close()).dimensions(this.width / 2 - 155, this.height-29, 150, 20).build()));
        widgets.add(ButtonWidget.builder(ScreenTexts.DONE, button -> applyAndClose()).dimensions(this.width / 2 - 155 + 160, this.height-29, 150, 20).build());

        widgets.forEach(this::addDrawableChild);
    }

    public void makeGridAreaVisable(){
        itemSelectButtons.forEach(button -> button.visible = true);
        leftArrowButton.visible = true;
        rightArrowButton.visible = true;
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
